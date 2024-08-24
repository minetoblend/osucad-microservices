package com.osucad.microservice

import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.metrics.micrometer.*
import io.micrometer.core.instrument.Clock
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.binder.MeterBinder
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.datadog.DatadogConfig
import io.micrometer.datadog.DatadogMeterRegistry
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import org.koin.ktor.ext.getKoin

@Module
class DatadogMetrics {

    @Single
    @Named("metrics-datadog")
    fun metricsPlugin(micrometerRegistry: MeterRegistry): ApplicationPlugin<Unit> {
        return createApplicationPlugin("DatadogMetrics") {

            val meterBinderList = application.getKoin().getAll<MeterBinder>()

            application.install(MicrometerMetrics) {
                registry = micrometerRegistry

                meterBinders = listOf(
                    *meterBinderList.toTypedArray(),
                    JvmMemoryMetrics(),
                    JvmGcMetrics(),
                    ProcessorMetrics()
                )
            }
        }
    }

    @Single
    fun meterRegistry(environment: ApplicationEnvironment): MeterRegistry {
        val datadogConfig = DatadogEnvironmentConfig(environment)

        return DatadogMeterRegistry(datadogConfig, Clock.SYSTEM)
    }

    private class DatadogEnvironmentConfig(
        val environment: ApplicationEnvironment,
    ) : DatadogConfig {
        override fun prefix(): String = "metrics.datadog"

        override fun get(key: String): String? = environment.config.tryGetString(key)

    }
}
