package com.osucad.gateway

import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException

class KtorWebsocketConnection(private val session: WebSocketServerSession) : WebSocketConnection {
    override suspend fun send(message: String) {
        session.send(message)
    }

    override suspend fun receiveText(): String {
        try {
            return when (val frame = session.incoming.receive()) {
                is Frame.Text -> frame.readText()
                else -> throw IllegalStateException("Expected text frame")
            }
        } catch (e: ClosedReceiveChannelException) {
            throw ConnectionClosedException()
        }
    }

    override suspend fun close() {
        session.close()
    }

}
