package com.osucad.microservice

import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import io.ktor.server.application.*
import io.ktor.server.config.*
import org.koin.dsl.module
import org.koin.ktor.plugin.koin


fun Application.configureRabbitMQ() {
    val connectionFactory = ConnectionFactory().apply {
        useNio()

        host = environment.config.tryGetString("rabbitmq.host") ?: "localhost"
        port = environment.config.tryGetString("rabbitmq.port")?.toInt() ?: 5672
        username = environment.config.tryGetString("rabbitmq.username") ?: "guest"
        password = environment.config.tryGetString("rabbitmq.password") ?: "guest"
    }

    koin {
        modules(
            module {
                single<Connection> { connectionFactory.newConnection() }
            }
        )
    }
}
