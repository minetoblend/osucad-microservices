package com.osucad.websocketgateway

import com.osucad.websocketgateway.plugins.configureSerialization
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main(args: Array<String>) = io.ktor.server.netty.EngineMain.main(args)

fun Application.main() {
    configureSerialization()

    routing {
        get {
            call.respondText("Hello, world!")
        }
    }
}