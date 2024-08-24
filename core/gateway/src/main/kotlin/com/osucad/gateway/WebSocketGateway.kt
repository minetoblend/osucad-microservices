package com.osucad.gateway

import com.github.michaelbull.logging.InlineLogger
import com.osucad.gateway.operations.OrderedOperationsSubscriber
import com.osucad.gateway.signals.SignalPublisher
import com.osucad.gateway.signals.SignalSubscriber
import com.osucad.protocol.*
import com.osucad.protocol.operations.UnorderedOperation
import dev.inmo.krontab.doInfinity
import io.micrometer.core.instrument.Timer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class WebSocketGateway(
    private val shardId: Int = 0,
    private val signalPublisher: SignalPublisher,
    private val signalSubscriber: SignalSubscriber,
    private val operationPublisher: FlowCollector<UnorderedOperation>,
    private val operationSubscriber: OrderedOperationsSubscriber,
    private val serializer: Json = Json,
    private val metrics: WebSocketGatewayMetrics = NoopWebSocketGatewayMetrics
) {
    private val connectedClients = ConcurrentHashMap<String, WebSocketClient>()

    private val logger = InlineLogger()

    private val clientIdGenerator = ClientIdGenerator(shardId)

    init {
        logger.info { "WebSocketGateway started with id: $shardId" }

        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        scope.launch {
            signalSubscriber.subscribe { signal ->
                broadcastLocal(signal.documentId, signal)
            }
        }

        scope.launch {
            operationSubscriber.subscribe { operations ->
                broadcastLocal(operations.documentId, operations)
            }
        }

        scope.launch {
            doInfinity("0 /5 * * *" /* Every 5 minutes */) {
                runGarbageCollection()
            }
        }
    }

    suspend fun accept(socket: WebSocketConnection) {
        val clientId = clientIdGenerator.next()

        val client = WebSocketClient(clientId, socket, serializer)

        connectedClients[clientId] = client

        logger.info { "Client connected with id: $clientId" }

        acceptMessages(client)

        onClose(client)
    }

    private suspend fun acceptMessages(client: WebSocketClient) {
        try {
            while (true) {
                val message = try {
                    client.receiveMessage()
                } catch (e: SerializationException) {
                    logger.debug(e) { "Failed to decode message from client: ${client.id}" }
                    client.close()
                    return
                }

                metrics.messageReceived(message.type, client.id)

                metrics.measureHandleMessage(message.type, client.id) {
                    handleMessage(client, message)
                }
            }
        } catch (e: ConnectionClosedException) {
            // Connection was closed, do nothing
        } catch (e: Exception) {
            e.printStackTrace()

            client.close()
        } finally {
            onClose(client)
        }
    }

    /**
     * Utility function that serializes the message before broadcasting it to prevent
     * multiple serializations of the same message.
     */
    private suspend fun broadcast(message: ServerMessage, clients: Collection<WebSocketClient>) {
        if (clients.isEmpty()) return

        val serialized = serializer.encodeToString(message)
        clients.forEach { client ->
            try {
                client.sendText(serialized)
            } catch (e: Exception) {
                logger.error(e) { "Failed to send message to client: ${client.id}" }
                forceDisconnect(client)
            }
        }
    }

    /**
     * Broadcast a message to all clients connected to a document. It will not broadcast to clients
     * of other shards.
     */
    private suspend fun broadcastLocal(documentId: String, message: ServerMessage) {
        broadcast(message, documents[documentId]?.clients ?: emptySet())
    }

    private suspend fun handleMessage(client: WebSocketClient, message: ClientMessage) {
        logger.debug { "Handling message from client: ${client.id}, message: $message" }

        when (message) {
            is ConnectDocumentRequest -> connectDocument(client, message)
            is DisconnectDocumentRequest -> disconnectDocument(client, message)
            is SubmitSignalRequest -> submitSignal(client, message)
            is SubmitOperation -> submitOperations(client, message)
        }
    }

    private suspend fun forceDisconnect(client: WebSocketClient) {
        client.close()
    }

    private suspend fun onClose(client: WebSocketClient) {
        connectedClients.remove(client.id)

        clientDocuments.remove(client.id)?.forEach { documentId ->
            runCatching {
                disconnectDocument(client, documentId)
            }.onFailure {
                logger.error(it) { "Failed to remove client from document" }
            }
        }
    }

    private suspend fun disconnectDocument(client: WebSocketClient, documentId: String) {
        val document = documents[documentId] ?: return

        if (document.clients.remove(client)) {
            clientDocuments[client.id]?.remove(documentId)

            signalPublisher.publish(
                SignalWithSender(
                    documentId = documentId,
                    clientId = client.id,
                    signal = UserLeft(client.id)
                )
            )
        }
    }

    private suspend fun submitOperations(client: WebSocketClient, message: SubmitOperation) {
        val document = documents[message.documentId] ?: return

        if (client !in document.clients)
            return

        operationPublisher.emit(
            UnorderedOperation(
                client.id,
                message.documentId,
                message.deltas,
            )
        )
    }

    private fun runGarbageCollection() {
        logger.debug { "Running garbage collection" }

        val keys = documents.keys.toList()

        var removed = 0

        keys.forEach { documentId ->
            documents.computeIfPresent(documentId) { _, document ->
                if (document.clients.isEmpty()) {
                    stopTrackingDocument(documentId)
                    removed++
                    null
                } else {
                    document
                }
            }
        }

        logger.debug { "Stopped tracking $removed documents without connected clients" }
    }

    inner class WebSocketClient(
        val id: String,
        private val connection: WebSocketConnection,
        private val serializer: Json,
    ) {
        suspend fun receiveMessage(): ClientMessage =
            serializer.decodeFromString<ClientMessage>(connection.receiveText())

        suspend fun close() = connection.close()

        internal suspend fun sendText(message: String) = connection.send(message)

        suspend fun send(message: ServerMessage) {
            sendText(serializer.encodeToString(message))
            metrics.messageSent(message.type, id)
        }

    }


    // region Message handlers

    private val documents = ConcurrentHashMap<String, DocumentInfo>()

    private val clientDocuments = ConcurrentHashMap<String, MutableSet<String>>()

    private suspend fun connectDocument(client: WebSocketClient, message: ConnectDocumentRequest) {
        val document = getOrCreateDocument(message.id)

        if (!document.clients.add(client)) {
            clientDocuments
                .computeIfAbsent(client.id) { Collections.synchronizedSet(mutableSetOf()) }
                .add(message.id)

            client.send(
                ConnectDocumentError(
                    message.seq,
                    message.id,
                    "Already connected to document"
                )
            )
            return
        }

        client.send(
            ConnectDocumentSuccess(
                message.seq,
                message.id
            )
        )

        signalPublisher.publish(
            documentId = message.id,
            clientId = null,
            signal = UserJoined(
                clientId = client.id
            ),
        )
    }

    private suspend fun disconnectDocument(
        client: WebSocketClient,
        message: DisconnectDocumentRequest
    ) = disconnectDocument(client, message.id)

    private suspend fun submitSignal(
        client: WebSocketClient,
        message: SubmitSignalRequest,
    ) {
        val document = this.documents[message.documentId] ?: return

        if (client !in document.clients)
            return

        val signal = SignalWithSender(
            documentId = message.documentId,
            clientId = client.id,
            signal = message.signal,
        )

        signalPublisher.publish(signal)
    }


    private fun getOrCreateDocument(id: String): DocumentInfo {
        var created = false
        val document = documents.computeIfAbsent(id) {
            created = true
            DocumentInfo(id)
        }

        if (created)
            startTrackingDocument(id)

        return document
    }

    private fun startTrackingDocument(documentId: String) {
        signalSubscriber.startTrackingDocument(documentId)
        operationSubscriber.startTrackingDocument(documentId)
    }

    private fun stopTrackingDocument(documentId: String) {
        signalSubscriber.stopTrackingDocument(documentId)
        operationSubscriber.stopTrackingDocument(documentId)
    }

    private class DocumentInfo(val id: String) {
        val clients: MutableSet<WebSocketClient> = Collections.synchronizedSet(mutableSetOf())
    }

    // endregion
}
