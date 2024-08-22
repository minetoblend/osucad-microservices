package com.osucad.websocketgateway.plugins

import io.ktor.server.application.*
import io.ktor.server.metrics.micrometer.*
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import org.koin.ktor.ext.getKoin

fun Application.configureMetrics() {
    val appMicrometerRegistry = getKoin().getOrNull<MeterRegistry>()

    if (appMicrometerRegistry === null) {
        log.info("No micrometer registry found, skipping metrics collection setup.")

        return
    }

    log.info("Configuring micrometer metrics with ${appMicrometerRegistry.javaClass.simpleName}")

    install(MicrometerMetrics) {
        registry = appMicrometerRegistry
        meterBinders =
            listOf(
                JvmMemoryMetrics(),
                JvmGcMetrics(),
                ProcessorMetrics(),
            )
    }
}
