package com.osucad.websocketgateway.monitoring

import io.ktor.server.application.*
import io.micrometer.core.instrument.Clock
import io.micrometer.datadog.DatadogConfig
import io.micrometer.datadog.DatadogMeterRegistry
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent

@Module
class DatadogMetricsModule : KoinComponent {
    @Single
    fun metricsConfigurer(environment: ApplicationEnvironment): MetricsConfiguration {
        val datadogConfig = DatadogEnvironmentConfig(environment)

        val registry = DatadogMeterRegistry(datadogConfig, Clock.SYSTEM)

        return MetricsConfiguration(registry)
    }

    private class DatadogEnvironmentConfig(
        val environment: ApplicationEnvironment,
    ) : DatadogConfig {
        override fun prefix(): String = "metrics.datadog"

        override fun get(key: String): String? = environment.config.propertyOrNull(key)?.getString()
    }
}
