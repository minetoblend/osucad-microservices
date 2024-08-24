package com.osucad.orderer

interface OrdererMetrics {
    suspend fun measureProcessDeltas(
        documentId: String,
        clientId: String,
        block: suspend () -> Unit
    )
}
