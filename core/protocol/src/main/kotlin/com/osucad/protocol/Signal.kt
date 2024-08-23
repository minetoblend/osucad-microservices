package com.osucad.protocol

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface SignalMessageBase {
    val clientId: String?
    val signal: Signal
    val targetClientId: String?
}

@Serializable
@SerialName("submit_signal")
data class SubmitSignal(
    val documentId: String,
    override val clientId: String,
    override val signal: Signal,
    override val targetClientId: String? = null,
) : ClientMessage, SignalMessageBase

@Serializable
@SerialName("signal")
data class ProcessedSignal(
    val documentId: String,
    override val clientId: String?,
    override val targetClientId: String?,
    override val signal: Signal,
) : ServerMessage, SignalMessageBase

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