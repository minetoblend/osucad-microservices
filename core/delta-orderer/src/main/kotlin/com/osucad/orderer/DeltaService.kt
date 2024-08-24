package com.osucad.orderer

interface DeltaService {
    suspend fun getLatestSequenceNumber(documentId: String): Long

    suspend fun setLatestSequenceNumber(documentId: String, sequenceNumber: Long)

    suspend fun withLock(documentId: String, block: suspend () -> Unit)
}
