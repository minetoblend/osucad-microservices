package com.osucad.gateway

import com.osucad.microservice.configureMicroservice
import com.osucad.microservice.configureRabbitMQ
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
    configureRabbitMQ()

    val signals = SignalPubSub(connection = get(), serializer = get())


    val gateway = WebSocketGateway(
        shardId = environment.config.tryGetString("ktor.application.shardId")?.toInt() ?: 0,
        serializer = get(),
        signalSubscriber = signals,
        signalPublisher = signals,
        operationPublisher = RabbitMQUnorderedOperationPublisher(get(), get()),
        operationSubscriber = RabbitMQOrderedOperationsSubscriber(get(), get()),
        metrics = MicrometerWebsocketGatewayMetrics(get())
    )

    routing {
        webSocket("/api/gateway") {
            val connection = KtorWebsocketConnection(this)

            gateway.accept(connection)
        }
    }
}


