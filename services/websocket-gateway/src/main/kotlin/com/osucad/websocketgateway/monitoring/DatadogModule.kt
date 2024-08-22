package com.osucad.websocketgateway.monitoring

import io.ktor.server.application.*
import io.micrometer.core.instrument.Clock
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.datadog.DatadogConfig
import io.micrometer.datadog.DatadogMeterRegistry
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent

@Module
class DatadogModule : KoinComponent {
    @Single
    fun datadogRegistry(environment: ApplicationEnvironment): MeterRegistry {
        val datadogConfig = EnvironmentConfig(environment)

        return DatadogMeterRegistry(datadogConfig, Clock.SYSTEM)
    }

    private class EnvironmentConfig(
        val environment: ApplicationEnvironment,
    ) : DatadogConfig {
        override fun prefix(): String = "metrics.datadog"

        override fun get(key: String): String? = environment.config.propertyOrNull(key)?.getString()
    }
}
