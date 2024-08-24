package com.osucad.gateway

interface WebSocketGatewayMetrics {

    suspend fun measureHandleMessage(
        messageType: String,
        clientId: String,
        block: suspend () -> Unit
    )

    suspend fun messageReceived(
        messageType: String,
        clientId: String
    )

    suspend fun messageSent(
        messageType: String,
        clientId: String
    )

}
