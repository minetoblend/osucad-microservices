package com.osucad.gateway

import com.osucad.gateway.operations.OrderedOperationsSubscriber
import com.osucad.protocol.OrderedOperation
import com.rabbitmq.client.Connection
import io.github.viartemev.rabbitmq.channel.consume
import kotlinx.serialization.json.Json

class RabbitMQOrderedOperationsSubscriber(
    connection: Connection,
    private val serializer: Json,
) : OrderedOperationsSubscriber {
    private val channel = connection.createChannel()

    private val exchange = "ordered-ops"

    init {
        channel.exchangeDeclare(exchange, "topic", true, false, null)
    }


    private val queue = channel.queueDeclare().queue

    override suspend fun subscribe(handler: suspend (OrderedOperation) -> Unit) {
        channel.consume(queue, 1) {
            consumeMessagesWithConfirm { delivery ->
                val message = String(delivery.body, charset("UTF-8"))

                val operation = serializer.decodeFromString<OrderedOperation>(message)

                handler(operation)
            }
        }
    }

    override fun startTrackingDocument(documentId: String) {
        channel.queueBind(queue, exchange, documentId)
    }

    override fun stopTrackingDocument(documentId: String) {
        channel.queueUnbind(queue, exchange, documentId)
    }
}
