package com.osucad.gateway.operations

import com.osucad.protocol.operations.UnorderedOperation

interface OperationPublisher {
    suspend fun publish(message: UnorderedOperation)
}
