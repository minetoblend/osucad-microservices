package com.osucad.gateway

object NoopWebSocketGatewayMetrics : WebSocketGatewayMetrics {
    override suspend fun measureHandleMessage(messageType: String, clientId: String, block: suspend () -> Unit) {
        block()
    }

    override suspend fun messageReceived(messageType: String, clientId: String) {
        // no-op
    }

    override suspend fun messageSent(messageType: String, clientId: String) {
        // no-op
    }
}
