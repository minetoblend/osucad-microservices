package com.osucad.microservice

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry


class PrometheusMetricsConfiguration : MetricsConfiguration {
    override val meterRegistry by lazy { PrometheusMeterRegistry(PrometheusConfig.DEFAULT) }
    override fun Application.configure() {
        routing {
            get("/metrics") {
                call.respond(meterRegistry.scrape())
            }
        }
    }


}
