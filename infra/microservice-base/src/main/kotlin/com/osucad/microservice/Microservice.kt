package com.osucad.microservice

import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.websocket.*
import org.koin.dsl.module
import org.koin.ktor.ext.getKoin
import org.koin.ktor.plugin.koin
import org.koin.logger.slf4jLogger


fun Application.configureMicroservice() {
    koin {
        slf4jLogger()
        allowOverride(true)
        modules(
            module {
                single { defaultSerializer() }
                single { environment }
            }
        )
    }

    configureSerialization()

    val metrics = when (environment.config.tryGetString("metrics.datadog.apiKey").isNullOrBlank()) {
        true -> PrometheusMetricsConfiguration()
        false -> DatadogMetricsConfiguration()
    }

    configureMetrics(metrics)


    for (plugin in getKoin().getAll<ApplicationPlugin<Unit>>()) {
        install(plugin)
    }
}
