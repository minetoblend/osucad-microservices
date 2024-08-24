package com.osucad.microservice

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonBuilder
import kotlinx.serialization.json.JsonNamingStrategy
import org.koin.ktor.ext.get

fun Application.configureSerialization() {
    val json = get<Json>()

    install(ContentNegotiation) {
        json(json)
    }
}

@OptIn(ExperimentalSerializationApi::class)
fun defaultSerializer(config: JsonBuilder.() -> Unit = {}) = Json {
    namingStrategy = JsonNamingStrategy.SnakeCase
    config()
}
