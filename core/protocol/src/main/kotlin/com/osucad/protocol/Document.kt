package com.osucad.protocol

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("connect_document")
class ConnectDocument(
    val id: String
) : ClientMessage

@Serializable
@SerialName("disconnect_document")
class DisconnectDocument(
    val id: String
) : ClientMessage

@Serializable
@SerialName("connect_document_success")
class ConnectDocumentSuccess(
    val id: String
) : ServerMessage
