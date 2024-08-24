package com.osucad.orderer

import com.github.michaelbull.logging.InlineLogger
import com.osucad.protocol.OrderedOperation
import com.osucad.protocol.operations.OrderedDeltas
import com.osucad.protocol.operations.UnorderedOperation
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.atomic.AtomicBoolean

class DeltaOrderer(
    private val deltaService: DeltaService,
    private val source: Flow<UnorderedOperation>,
    private val destination: FlowCollector<OrderedOperation>,
    private val metrics: OrdererMetrics = NoopOrdererMetrics
) {
    private val logger = InlineLogger()

    init {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        scope.launch {
            logger.info { "Starting DeltaOrderer" }

            source.collect { message ->
                metrics.measureProcessDeltas(
                    message.documentId,
                    message.clientId
                ) {
                    processDeltas(message)
                }
            }
        }
    }


    private suspend fun processDeltas(message: UnorderedOperation) {
        deltaService.withLock(message.documentId) {
            var sequenceNumber = deltaService.getLatestSequenceNumber(message.documentId)

            val ordered = message.deltas.map { operation ->
                OrderedDeltas(
                    sequenceNumber = ++sequenceNumber,
                    clientSequenceNumber = operation.clientSequenceNumber,
                    clientId = message.clientId,
                    content = operation.content,
                )
            }

            destination.emit(OrderedOperation(message.documentId, ordered))

            deltaService.setLatestSequenceNumber(message.documentId, sequenceNumber)
        }
    }
}
