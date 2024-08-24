package com.osucad.microservice

import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.websocket.*
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.get
import java.time.Duration

fun Application.configureWebSockets(config: WebSockets.WebSocketOptions.() -> Unit = {}) {
    val json = get<Json>()

    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(30)
        contentConverter = KotlinxWebsocketSerializationConverter(json)

        config()
    }
}
