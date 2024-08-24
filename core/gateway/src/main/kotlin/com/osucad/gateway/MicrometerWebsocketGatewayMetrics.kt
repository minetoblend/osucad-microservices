package com.osucad.gateway

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer

class MicrometerWebsocketGatewayMetrics(registry: MeterRegistry) : WebSocketGatewayMetrics {
    private val receivedMessageCounter = Counter
        .builder("osucad.gateway.messages.received")
        .description("Number of messages sent and received")
        .baseUnit("messages")
        .withRegistry(registry)

    private val sentMessageCounter = Counter
        .builder("osucad.gateway.messages.sent")
        .description("Number of messages sent and received")
        .baseUnit("messages")
        .withRegistry(registry)

    private val messageHandleDuration = Timer
        .builder("osucad.gateway.messages.handled")
        .description("Time taken to handle websocket messages")
        .withRegistry(registry)

    override suspend fun measureHandleMessage(messageType: String, clientId: String, block: suspend () -> Unit) {
        val sample = Timer.start()

        try {
            block()
        } finally {
            sample.stop(
                messageHandleDuration.withTags(
                    "messageType", messageType,
                    "clientId", clientId
                )
            )
        }
    }

    override suspend fun messageReceived(messageType: String, clientId: String) {
        receivedMessageCounter
            .withTags("messageType", messageType, "clientId", clientId)
            .increment()
    }

    override suspend fun messageSent(messageType: String, clientId: String) {
        sentMessageCounter
            .withTags("messageType", messageType, "clientId", clientId)
            .increment()
    }


}
