package com.osucad.orderer

import com.osucad.protocol.operations.UnorderedOperation

interface DeltaSource {
    suspend fun poll(): UnorderedOperation?
}
