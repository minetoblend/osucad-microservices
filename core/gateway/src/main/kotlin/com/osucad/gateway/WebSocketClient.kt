package com.osucad.gateway

import com.osucad.protocol.ClientMessage
import com.osucad.protocol.ServerMessage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class WebSocketClient(
    val id: String,
    private val connection: WebSocketConnection,
    private val serializer: Json,
) {

    suspend fun receiveMessage(): ClientMessage =
        serializer.decodeFromString<ClientMessage>(connection.receiveText())

    suspend fun close() = connection.close()

    suspend internal fun sendText(message: String) = connection.send(message)

    suspend fun send(message: ServerMessage) = sendText(serializer.encodeToString(message))

}
