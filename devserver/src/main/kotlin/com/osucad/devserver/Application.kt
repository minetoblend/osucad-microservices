package com.osucad.devserver

import com.osucad.gateway.MicrometerWebsocketGatewayMetrics
import com.osucad.gateway.WebSocketGateway
import com.osucad.microservice.configureMicroservice
import com.osucad.microservice.configureWebSockets
import com.osucad.orderer.DeltaOrderer
import com.osucad.orderer.InMemoryDeltaService
import com.osucad.orderer.MicrometerOrdererMetrics
import com.osucad.protocol.OrderedOperation
import com.osucad.protocol.operations.UnorderedOperation
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.koin.ktor.ext.get

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.main() {
    configureMicroservice()
    configureWebSockets()

    val signalPubSub = SignalPubSub()


    val orderedDeltas = MutableSharedFlow<OrderedOperation>()

    val unorderedDeltas = MutableSharedFlow<UnorderedOperation>()

    val deltaService = InMemoryDeltaService()

    val gateway = WebSocketGateway(
        serializer = get(),
        signalSubscriber = signalPubSub,
        signalPublisher = signalPubSub,
        operationSubscriber = OrderedOperationFlowSubscriber(orderedDeltas.asSharedFlow()),
        operationPublisher = unorderedDeltas,
        metrics = MicrometerWebsocketGatewayMetrics(get())
    )

    DeltaOrderer(
        deltaService = deltaService,
        destination = orderedDeltas,
        source = unorderedDeltas.asSharedFlow(),
        metrics = MicrometerOrdererMetrics(get())
    )

    routing {
        webSocket("/api/gateway") {
            val connection = KtorWebsocketConnection(this)

            gateway.accept(connection)
        }
    }
}
