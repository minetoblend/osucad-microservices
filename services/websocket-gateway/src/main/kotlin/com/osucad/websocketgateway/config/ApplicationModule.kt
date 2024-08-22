package com.osucad.websocketgateway.config

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonBuilder
import kotlinx.serialization.json.JsonNamingStrategy
import org.koin.core.annotation.Single

interface ApplicationModule {
    @OptIn(ExperimentalSerializationApi::class)
    @Single
    fun jsonSerializer() =
        Json {
            configureJson()
            encodeDefaults = true
            namingStrategy = JsonNamingStrategy.SnakeCase
        }

    fun JsonBuilder.configureJson() {}
}