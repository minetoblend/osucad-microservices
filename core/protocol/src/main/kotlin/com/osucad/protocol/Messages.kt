package com.osucad.protocol

import kotlinx.serialization.Serializable

@Serializable
sealed interface ClientMessage {
    val type: String get() = this::class.simpleName!!
}

@Serializable
sealed interface ServerMessage {
    val type: String get() = this::class.simpleName!!
}
