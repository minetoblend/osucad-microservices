package com.osucad.microservice

import io.ktor.server.application.*
import io.ktor.server.config.*
import io.micrometer.core.instrument.Clock
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.datadog.DatadogConfig
import io.micrometer.datadog.DatadogMeterRegistry
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class DatadogMetricsConfiguration : MetricsConfiguration, KoinComponent {
    override val meterRegistry by lazy {
        val datadogConfig = DatadogEnvironmentConfig(get())

        DatadogMeterRegistry(datadogConfig, Clock.SYSTEM)
    }
}

private class DatadogEnvironmentConfig(
    val environment: ApplicationEnvironment,
) : DatadogConfig {
    override fun prefix(): String = "metrics.datadog"

    override fun get(key: String): String? = environment.config.tryGetString(key)
}
