package com.osucad.websocketgateway.plugins

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.get

fun Application.configureSerialization() {
    val jsonConfig = get<Json>()

    install(ContentNegotiation) {
        json(jsonConfig)
    }
}
