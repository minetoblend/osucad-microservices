package com.osucad.websocketgateway.services

import com.osucad.protocol.ClientMessage
import com.osucad.protocol.ConnectDocument
import com.osucad.protocol.ConnectDocumentSuccess
import com.osucad.protocol.ConnectInfo
import com.osucad.protocol.DisconnectDocument
import com.osucad.protocol.ProcessedSignal
import com.osucad.protocol.ServerMessage
import com.osucad.protocol.SubmitSignal
import com.osucad.protocol.UserJoined
import com.osucad.protocol.UserLeft
import io.ktor.server.websocket.*
import io.ktor.util.collections.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import org.slf4j.LoggerFactory
import java.util.Collections
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.set

@Single
class WebSocketService(
    private val json: Json,
    private val signalService: SignalService,
) : KoinComponent {
    private val logger = LoggerFactory.getLogger(WebSocketService::class.java)

    private val connectedClients = ConcurrentMap<String, Client>()

    private val serverId = UUID.randomUUID()

    private val clientIdCounter = AtomicInteger(0)

    private val documents = ConcurrentMap<String, DocumentInfo>()

    init {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        scope.launch {
            signalService.onSignal(::broadcastSignal)
        }
    }

    private fun createClientId(): String {
        val id = clientIdCounter.incrementAndGet()
        return "$serverId-$id"
    }

    suspend fun onConnection(session: DefaultWebSocketServerSession) {
        val client = Client(createClientId(), session)

        connectedClients[client.id] = client

        try {
            client.send(ConnectInfo(client.id))

            while (true) {
                val message = try {
                    client.receiveMessage()
                } catch (e: Exception) {
                    logger.error("Error receiving message", e)
                    session.closeExceptionally(e)
                    break
                }

                onMessage(client, message)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        onClose(client, client.closeReason.await())
    }

    private suspend fun onClose(client: Client, reason: CloseReason?) {
        connectedClients.remove(client.id)

        for (documentId in client.connectedDocuments) {
            disconnectDocument(client, DisconnectDocument(documentId))
        }
    }

    private suspend fun onMessage(client: Client, message: ClientMessage) {
        when (message) {
            is ConnectDocument -> connectDocument(client, message)
            is DisconnectDocument -> disconnectDocument(client, message)
            is SubmitSignal -> submitSignal(client, message)
        }
    }


    private suspend fun connectDocument(client: Client, message: ConnectDocument) {
        val documentId = message.id

        logger.info("Attempting to connect client {} to document {}", client.id, documentId)

        val document = documents.computeIfAbsent(documentId) {
            signalService.subscribeToDocument(documentId)

            DocumentInfo(documentId)
        }

        if (document.addClient(client)) {
            client.connectedDocuments.add(documentId)

            client.send(ConnectDocumentSuccess(documentId))


            signalService.publishSignal(
                ProcessedSignal(
                    documentId = documentId,
                    clientId = null,
                    signal = UserJoined(client.id),
                    targetClientId = null,
                )
            )

            logger.info("Client {} connected to document {}", client.id, documentId)

        }
    }

    private suspend fun disconnectDocument(client: Client, message: DisconnectDocument) {
        val documentId = message.id

        val document = documents[documentId] ?: return

        document.removeClient(client)

        broadcastSignal(
            ProcessedSignal(
                documentId = documentId,
                clientId = client.id,
                signal = UserLeft(client.id),
                targetClientId = null,
            )
        )

        synchronized(this) {
            if (document.hasNoClients) {
                documents.remove(documentId)
                signalService.unsubscribeFromDocument(documentId)
            }
        }
    }

    private suspend fun submitSignal(client: Client, request: SubmitSignal) {
        val documentId = request.documentId

        logger.info("Signal submitted by client {}", client.id)

        signalService.publishSignal(
            ProcessedSignal(
                documentId = documentId,
                clientId = client.id,
                signal = request.signal,
                targetClientId = request.targetClientId,
            )
        )
    }

    private class DocumentInfo(val id: String) {
        val clients: MutableSet<Client> = Collections.synchronizedSet(mutableSetOf<Client>())

        val hasNoClients: Boolean get() = clients.isEmpty()

        fun addClient(client: Client): Boolean {
            val result = clients.add(client)

            if (result)
                client.connectedDocuments.add(id)

            return result
        }

        fun removeClient(client: Client) {
            clients.remove(client)
            client.connectedDocuments.remove(id)
        }
    }

    private suspend fun broadcastSignal(signal: ProcessedSignal) {
        if (signal.targetClientId != null) {
            val client = connectedClients[signal.targetClientId] ?: return

            if (signal.documentId !in client.connectedDocuments)
                return

            client.send(signal)

            return
        }

        val document = documents[signal.documentId] ?: return

        document.clients.forEach { it.send(signal) }
    }


    inner class Client(
        val id: String,
        private val session: DefaultWebSocketServerSession,
    ) {
        val connectedDocuments: MutableSet<String> = Collections.synchronizedSet(mutableSetOf<String>())

        suspend fun receiveText() = when (val frame = session.incoming.receive()) {
            is Frame.Text -> frame.readText()
            else -> throw IllegalStateException("Unsupported frame type: $frame")
        }

        suspend fun receiveMessage() = json.decodeFromString<ClientMessage>(receiveText())

        suspend fun send(message: ServerMessage) = session.send(json.encodeToString(message))

        suspend fun close() = session.close()

        val closeReason get() = session.closeReason
    }
}