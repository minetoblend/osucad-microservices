package com.osucad.gateway

import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import io.ktor.server.application.*
import io.ktor.server.config.*
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class RabbitMQModule {
    @Single
    fun rabbitmqConnection(environment: ApplicationEnvironment): Connection {
        val connectionFactory = ConnectionFactory().apply {
            useNio()

            host = environment.config.tryGetString("rabbitmq.host") ?: "localhost"
            port = environment.config.tryGetString("rabbitmq.port")?.toInt() ?: 5672
            username = environment.config.tryGetString("rabbitmq.username") ?: "guest"
            password = environment.config.tryGetString("rabbitmq.password") ?: "guest"
        }

        return connectionFactory.newConnection()
    }
}
