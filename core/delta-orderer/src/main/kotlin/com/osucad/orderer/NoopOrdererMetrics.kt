package com.osucad.orderer

object NoopOrdererMetrics : OrdererMetrics {
    override suspend fun measureProcessDeltas(
        documentId: String,
        clientId: String,
        block: suspend () -> Unit,
    ) {
        block()
    }
}
