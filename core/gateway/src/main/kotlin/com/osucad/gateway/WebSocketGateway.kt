package com.osucad.gateway

import com.github.michaelbull.logging.InlineLogger
import com.osucad.gateway.signals.SignalPublisher
import com.osucad.gateway.signals.SignalSubscriber
import com.osucad.protocol.*
import dev.inmo.krontab.doInfinity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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
    private val serializer: Json = Json,
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
            doInfinity("0 /5 * * *" /* Every 5 minutes */) {
                runGarbageCollection()
            }
        }
    }

    suspend fun onConnection(socket: WebSocketConnection) {
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

                handleMessage(client, message)
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
        clients.forEach { it.sendText(serialized) }
    }

    private suspend fun broadcastLocal(documentId: String, message: ServerMessage) {
        broadcast(message, documents[documentId]?.clients ?: emptySet())
    }

    private suspend fun handleMessage(client: WebSocketClient, message: ClientMessage) {
        logger.debug { "Handling message from client: ${client.id}, message: $message" }

        when (message) {
            is ConnectDocumentRequest -> connectDocument(client, message)
            is DisconnectDocumentRequest -> disconnectDocument(client, message)
            is SubmitSignalRequest -> submitSignal(client, message)
        }
    }

    private fun onClose(client: WebSocketClient) {
        connectedClients.remove(client.id)

        clientDocuments.remove(client.id)?.forEach { documentId ->
            documents[documentId]?.clients?.remove(client)
        }
    }

    private fun runGarbageCollection() {
        logger.debug { "Running garbage collection" }

        val keys = documents.keys.toList()

        var removed = 0

        keys.forEach { documentId ->
            documents.computeIfPresent(documentId) { _, document ->
                if (document.clients.isEmpty()) {
                    signalSubscriber.stopTrackingDocument(documentId)
                    removed++
                    null
                } else {
                    document
                }
            }
        }

        logger.debug { "Stopped tracking $removed documents without connected clients" }
    }

    // region Message handlers

    private val documents = ConcurrentHashMap<String, DocumentInfo>()

    private val clientDocuments = ConcurrentHashMap<String, MutableSet<String>>()

    private suspend fun connectDocument(client: WebSocketClient, message: ConnectDocumentRequest) {
        val document = documents.computeIfAbsent(message.id) {
            DocumentInfo(message.id)
        }

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

    private suspend fun disconnectDocument(client: WebSocketClient, message: DisconnectDocumentRequest) {
        val document = documents[message.id] ?: return

        if (document.clients.remove(client)) {
            clientDocuments[client.id]?.remove(message.id)

            signalPublisher.publish(
                documentId = message.id,
                clientId = null,
                signal = UserLeft(
                    clientId = client.id
                ),
            )


            if (document.clients.isEmpty()) {
                documents.remove(message.id)
                signalSubscriber.stopTrackingDocument(message.id)
            }
        }
    }

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
            signalSubscriber.startTrackingDocument(id)

        return document
    }

    private class DocumentInfo(val id: String) {
        val clients: MutableSet<WebSocketClient> = Collections.synchronizedSet(mutableSetOf())
    }

    // endregion
}
