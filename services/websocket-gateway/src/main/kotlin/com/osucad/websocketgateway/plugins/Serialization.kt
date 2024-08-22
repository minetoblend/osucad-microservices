package com.osucad.websocketgateway.plugins

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy

@OptIn(ExperimentalSerializationApi::class)
fun Application.configureSerialization() {

    fun getJsonConfig(): Json {
        if (environment.developmentMode) {
            return Json {
                namingStrategy = JsonNamingStrategy.SnakeCase
                prettyPrint = true
            }
        }

        return Json {
            namingStrategy = JsonNamingStrategy.SnakeCase
        }
    }

    install(ContentNegotiation) {
        json(getJsonConfig())
    }
}
