package com.osucad.devserver

import com.osucad.gateway.operations.OrderedOperationsSubscriber
import com.osucad.protocol.OrderedOperation
import kotlinx.coroutines.flow.Flow

class OrderedOperationFlowSubscriber(
    private val flow: Flow<OrderedOperation>
) : OrderedOperationsSubscriber {
    override suspend fun subscribe(handler: suspend (OrderedOperation) -> Unit) {
        flow.collect(handler)
    }

    override fun startTrackingDocument(documentId: String) {
        // no-op
    }

    override fun stopTrackingDocument(documentId: String) {
        // no-op
    }

}
