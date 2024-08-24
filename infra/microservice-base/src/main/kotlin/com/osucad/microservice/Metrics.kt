package com.osucad.microservice

import io.ktor.server.application.*
import io.ktor.server.metrics.micrometer.*
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.binder.MeterBinder
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import org.koin.dsl.module
import org.koin.ktor.plugin.koin

interface MetricsConfiguration {
    val meterRegistry: MeterRegistry

    fun meterBinders(): List<MeterBinder> = listOf(
        JvmMemoryMetrics(),
        JvmGcMetrics(),
        ProcessorMetrics()
    )

    fun Application.configure() {}
}

fun Application.configureMetrics(config: MetricsConfiguration) {
    install(MicrometerMetrics) {
        registry = config.meterRegistry
        meterBinders = config.meterBinders()
    }

    koin {
        modules(
            module {
                single { config.meterRegistry }
            }
        )
    }

    with(config) {
        configure()
    }
}
