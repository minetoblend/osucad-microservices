package com.osucad.websocketgateway.routes

import com.osucad.websocketgateway.services.WebSocketService
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import org.koin.ktor.ext.get

fun Route.gatewayRoute() {
    val wsService = get<WebSocketService>()

    webSocket("/gateway") {
        wsService.onConnection(this)
    }
}