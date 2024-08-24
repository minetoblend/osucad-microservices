package com.osucad.devserver

import com.osucad.gateway.WebSocketGateway
import com.osucad.gateway.WebsocketGatewayMetrics
import com.osucad.microservice.configureMicroservice
import com.osucad.microservice.configureWebSockets
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import org.koin.ktor.ext.get

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.main() {
    configureMicroservice()
    configureWebSockets()

    val signalPubSub = SignalPubSub()

    val gateway = WebSocketGateway(
        serializer = get(),
        signalSubscriber = signalPubSub,
        signalPublisher = signalPubSub,
    )

    routing {
        webSocket("/api/gateway") {
            val connection = KtorWebsocketConnection(this)

            gateway.accept(connection)
        }
    }
}
