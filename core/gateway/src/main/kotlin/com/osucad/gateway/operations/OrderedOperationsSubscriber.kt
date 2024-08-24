package com.osucad.gateway.operations

import com.osucad.protocol.OrderedOperation

interface OrderedOperationsSubscriber {
    suspend fun subscribe(handler: suspend (OrderedOperation) -> Unit)

    fun startTrackingDocument(documentId: String)

    fun stopTrackingDocument(documentId: String)
}
