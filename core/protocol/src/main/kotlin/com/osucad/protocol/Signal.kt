package com.osucad.protocol

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName("submit_signal")
data class SubmitSignalRequest(
    val documentId: String,
    val signal: Signal,
    val targetClientId: String? = null,
) : ClientMessage

@Serializable
@SerialName("signal")
data class SignalWithSender(
    val documentId: String,
    val clientId: String?,
    val signal: Signal,
    val targetClientId: String? = null,
) : ServerMessage

@Serializable
sealed interface Signal

@Serializable
@SerialName("user_joined")
class UserJoined(
    val clientId: String,
) : Signal

@Serializable
@SerialName("user_left")
class UserLeft(
    val clientId: String,
) : Signal
