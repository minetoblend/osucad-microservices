package com.osucad.gateway

interface WebSocketConnection {
    abstract suspend fun send(message: String)

    @Throws(ConnectionClosedException::class)
    abstract suspend fun receiveText(): String

    abstract suspend fun close()
}
