package com.osucad.gateway

import com.osucad.gateway.signals.SignalPublisher
import com.osucad.gateway.signals.SignalSubscriber
import com.osucad.protocol.SignalWithSender
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Connection
import io.github.viartemev.rabbitmq.channel.consume
import io.github.viartemev.rabbitmq.channel.createConfirmChannel
import io.github.viartemev.rabbitmq.channel.publish
import io.github.viartemev.rabbitmq.publisher.OutboundMessage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single
import org.slf4j.LoggerFactory

@Single
class SignalPubSub(
    connection: Connection,
    private val serializer: Json,
) : SignalPublisher, SignalSubscriber {
    private val channel = connection.createConfirmChannel()

    private val queue = channel.queueDeclare().queue

    private val logger = LoggerFactory.getLogger(SignalPubSub::class.java)

    companion object {
        const val SIGNALS_EXCHANGE = "signals"
    }

    init {
        logger.debug("SignalService listening on queue: {}", queue)

        channel.exchangeDeclare(SIGNALS_EXCHANGE, "topic")
    }

    override suspend fun publish(signal: SignalWithSender) {
        logger.info("Publishing signal: {}", signal)

        val message = serializer.encodeToString(signal)

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

    override suspend fun subscribe(block: suspend (signal: SignalWithSender) -> Unit) {
        channel.consume(queue, 1) {
            consumeMessagesWithConfirm { delivery ->
                val message = String(delivery.body, charset("UTF-8"))

                val signal = serializer.decodeFromString<SignalWithSender>(message)

                block(signal)
            }
        }
    }


    override fun startTrackingDocument(documentId: String) {
        logger.info("Subscribing to document {}", documentId)
        channel.queueBind(queue, SIGNALS_EXCHANGE, documentId)
    }

    override fun stopTrackingDocument(documentId: String) {
        logger.info("Unsubscribing from document {}", documentId)
        channel.queueUnbind(queue, SIGNALS_EXCHANGE, documentId)
    }

}

