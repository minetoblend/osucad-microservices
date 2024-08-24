package com.osucad.gateway

import com.osucad.protocol.operations.UnorderedOperation
import com.rabbitmq.client.Connection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


class RabbitMQUnorderedOperationPublisher(
    connection: Connection,
    private val serializer: Json,
) : FlowCollector<UnorderedOperation> {
    private val _messages = MutableSharedFlow<UnorderedOperation>()
    private val messages = _messages.asSharedFlow()

    override suspend fun emit(value: UnorderedOperation) {
        _messages.emit(value)
    }

    private val channel = connection.createChannel()

    private val queue = channel.queueDeclare("unordered-ops", true, false, false, null).queue

    init {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        scope.launch {
            messages.collect { message ->
                val serialized = serializer.encodeToString(message)

                channel.basicPublish("", queue, null, serialized.toByteArray())
            }
        }
    }
}
