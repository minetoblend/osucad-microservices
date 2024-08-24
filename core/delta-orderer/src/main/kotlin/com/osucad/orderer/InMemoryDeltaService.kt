package com.osucad.orderer

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

class InMemoryDeltaService : DeltaService {
    private val sequenceNumbers = ConcurrentHashMap<String, Long>()

    private val documentLocks = ConcurrentHashMap<String, Mutex>()

    override suspend fun getLatestSequenceNumber(documentId: String): Long {
        return sequenceNumbers[documentId] ?: 0
    }

    override suspend fun setLatestSequenceNumber(documentId: String, sequenceNumber: Long) {
        sequenceNumbers[documentId] = sequenceNumber
    }

    override suspend fun withLock(documentId: String, block: suspend () -> Unit) {
        val mutex = documentLocks.computeIfAbsent(documentId) { Mutex() }

        mutex.withLock {
            block()
        }
    }

}
