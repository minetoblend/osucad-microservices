package com.osucad.gateway

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer

class WebsocketGatewayMetrics(registry: MeterRegistry) {
    val receivedMessageCounter = Counter
        .builder("osucad.gateway.messages.received")
        .description("Number of messages sent and received")
        .baseUnit("messages")
        .withRegistry(registry)

    val sentMessageCounter = Counter
        .builder("osucad.gateway.messages.sent")
        .description("Number of messages sent and received")
        .baseUnit("messages")
        .withRegistry(registry)

    val messageHandleDuration = Timer
        .builder("osucad.gateway.messages.handled")
        .description("Time taken to handle websocket messages")
        .withRegistry(registry)

}
