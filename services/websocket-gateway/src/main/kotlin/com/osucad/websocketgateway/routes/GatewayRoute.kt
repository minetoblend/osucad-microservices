package com.osucad.websocketgateway.routes

import com.osucad.gateway.ConnectionClosedException
import com.osucad.gateway.WebSocketConnection
import com.osucad.gateway.WebSocketGateway
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import org.koin.ktor.ext.get

fun Route.gatewayRoute() {
    val gateway = WebSocketGateway(
        serializer = get(),
        signalSubscriber = get(),
        signalPublisher = get()
    )

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

    webSocket("/gateway") {
        gateway.onConnection(KtorWebsocketConnection(this))
    }
}
