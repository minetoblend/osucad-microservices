package com.osucad.websocketgateway.services

import com.osucad.protocol.ProcessedSignal
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Connection
import com.rabbitmq.client.DeliverCallback
import io.github.viartemev.rabbitmq.channel.consume
import io.github.viartemev.rabbitmq.channel.createConfirmChannel
import io.github.viartemev.rabbitmq.channel.publish
import io.github.viartemev.rabbitmq.publisher.OutboundMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single
import org.slf4j.LoggerFactory
import kotlin.coroutines.coroutineContext

@Single
class SignalService(
    connection: Connection,
    private val json: Json,
) {
    private val channel = connection.createConfirmChannel()

    private val queue = channel.queueDeclare().queue

    private val logger = LoggerFactory.getLogger(SignalService::class.java)

    companion object {
        const val SIGNALS_EXCHANGE = "signals"
    }

    init {
        logger.debug("SignalService listening on queue: {}", queue)

        channel.exchangeDeclare(SIGNALS_EXCHANGE, "topic")
    }

    suspend fun onSignal(block: suspend (signal: ProcessedSignal) -> Unit) {

        channel.consume(queue, 1) {
            consumeMessagesWithConfirm { delivery ->
                val message = String(delivery.body, charset("UTF-8"))

                val signal = json.decodeFromString<ProcessedSignal>(message)

                block(signal)
            }
        }

        return

        val scope = CoroutineScope(coroutineContext)

        val flow = MutableSharedFlow<ProcessedSignal>()

        channel.basicConsume(queue, DeliverCallback { _, delivery ->
            val message = String(delivery.body, charset("UTF-8"))

            val signal = Json.decodeFromString<ProcessedSignal>(message)

            scope.launch { flow.emit(signal) }
        }) { _ -> }

        flow.asSharedFlow().collect { signal ->
            block(signal)
        }
    }

    suspend fun publishSignal(signal: ProcessedSignal) {
        logger.info("Publishing signal: {}", signal)

        val message = json.encodeToString(signal)

        channel.publish {
            publishWithConfirm(
                OutboundMessage(
                    SIGNALS_EXCHANGE,
                    signal.documentId,
                    AMQP.BasicProperties(),
                    message.toByteArray()
                )
            )
        }
    }

    fun subscribeToDocument(documentId: String) {
        logger.info("Subscribing to document {}", documentId)
        channel.queueBind(queue, SIGNALS_EXCHANGE, documentId)
    }

    fun unsubscribeFromDocument(documentId: String) {
        logger.info("Unsubscribing from document {}", documentId)
        channel.queueUnbind(queue, SIGNALS_EXCHANGE, documentId)
    }

}

