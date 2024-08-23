package com.osucad.websocketgateway.plugins

import com.osucad.websocketgateway.routes.gatewayRoute
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        route("/api") {
            gatewayRoute()
        }
    }
}
