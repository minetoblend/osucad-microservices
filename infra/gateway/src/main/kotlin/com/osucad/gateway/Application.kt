package com.osucad.gateway

import com.osucad.microservice.DatadogMetrics
import com.osucad.microservice.DefaultModule
import com.osucad.microservice.configureWebSockets
import com.osucad.microservice.startMicroservice
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.micrometer.core.instrument.MeterRegistry
import org.koin.ksp.generated.module
import org.koin.ktor.ext.get
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.main() {
    install(Koin) {
        slf4jLogger()
        modules(
            DefaultModule().module,
            DatadogMetrics().module,
            RabbitMQModule().module
        )
    }

    startMicroservice {
        configureWebSockets()

        val signals = SignalPubSubService(
            connection = get(),
            json = get()
        )

        val gateway = WebSocketGateway(
            serializer = get(),
            signalSubscriber = signals,
            signalPublisher = signals,
            metrics = WebsocketGatewayMetrics(get())
        )

        get<MeterRegistry>().forEachMeter {
            println(it.id)
        }

        routing {
            route("/api") {
                webSocket("/gateway") {
                    val connection = KtorWebsocketConnection(this)

                    gateway.accept(connection)
                }
            }
        }
    }
}


