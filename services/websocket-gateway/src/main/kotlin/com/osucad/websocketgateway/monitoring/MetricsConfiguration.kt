package com.osucad.websocketgateway.monitoring

import com.osucad.websocketgateway.utils.inlineLogger
import io.ktor.serialization.*
import io.ktor.server.application.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.util.*
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics

open class MetricsConfiguration(val micrometerRegistry: MeterRegistry) :
    BaseApplicationPlugin<Application, Configuration, MetricsConfiguration> {

    protected val logger = inlineLogger()

    override val key: AttributeKey<MetricsConfiguration> = AttributeKey("MetricsConfiguration")

    private var installed = false
    override fun install(application: Application, configure: Configuration.() -> Unit): MetricsConfiguration {
        if (installed)
            throw IllegalStateException("${this::class.java.simpleName} is already installed")

        installed = true

        application.install(MicrometerMetrics) {
            configureMetrics()
        }

        application.configureApplication()

        return this
    }

    protected open fun MicrometerMetricsConfig.configureMetrics() {
        logger.info("Configuring micrometer metrics with {}", micrometerRegistry::class.java.simpleName)
        registry = micrometerRegistry

        logger.info("Using default meter bindings")
        meterBinders = listOf(
            JvmMemoryMetrics(),
            JvmGcMetrics(),
            ProcessorMetrics(),
        )
    }

    protected open fun Application.configureApplication() {}

}
