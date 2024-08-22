package com.osucad.websocketgateway.plugins

import dev.hayden.KHealth
import io.ktor.server.application.*

fun Application.configureHealthChecks() {
    install(KHealth)
}
