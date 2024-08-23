package com.osucad.websocketgateway.services

import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan
class ServiceModule {


    @Single
    fun rabbitmqConnection(): Connection {
        val connectionFactory = ConnectionFactory().apply {
            useNio()
            host = "localhost"
            port = 5672
        }

        return connectionFactory.newConnection()
    }

}