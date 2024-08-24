package com.osucad.orderer

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.Timer

class MicrometerOrdererMetrics(
    private val registry: MeterRegistry,
    private val tags: List<Tag> = emptyList()
) : OrdererMetrics {

    val timer = Timer.builder("osucad.orderer.process-deltas")
        .description("Time taken to process deltas")
        .tags(tags)
        .withRegistry(registry)

    override suspend fun measureProcessDeltas(documentId: String, clientId: String, block: suspend () -> Unit) {
        val sample = Timer.start()

        try {
            block()
        } finally {
            sample.stop(
                timer.withTags(
                    "documentId", documentId,
                    "clientId", clientId
                )
            )
        }
    }
}
