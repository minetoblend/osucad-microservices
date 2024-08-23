package com.osucad.protocol

import kotlinx.serialization.Serializable

@Serializable
sealed interface ClientMessage

@Serializable
sealed interface ServerMessage