package com.osucad.websocketgateway.plugins

import com.osucad.websocketgateway.monitoring.MetricsConfiguration
import io.ktor.server.application.*
import org.koin.ktor.ext.getKoin

fun Application.configureMetrics() {
    val config = getKoin().getOrNull<MetricsConfiguration>()

    if (config === null) {
        log.info("No metrics configuration found, skipping metrics collection setup.")

        return
    }

    install(config)
}
