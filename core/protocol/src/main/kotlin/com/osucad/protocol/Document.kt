package com.osucad.protocol

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("connect_document")
class ConnectDocumentRequest(
    val seq: Long,
    val id: String,
    val token: String? = null
) : ClientMessage

@Serializable
@SerialName("disconnect_document")
class DisconnectDocumentRequest(
    val seq: Long,
    val id: String
) : ClientMessage

@Serializable
@SerialName("connect_document_success")
class ConnectDocumentSuccess(
    val seq: Long,
    val id: String,
) : ServerMessage

@Serializable
@SerialName("connect_document_error")
class ConnectDocumentError(
    val seq: Long,
    val id: String,
    val error: String,
) : ServerMessage
