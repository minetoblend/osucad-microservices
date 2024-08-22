package com.osucad.websocketgateway.plugins

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get {
            call.respondText("Hello, world!")
        }
    }
}
