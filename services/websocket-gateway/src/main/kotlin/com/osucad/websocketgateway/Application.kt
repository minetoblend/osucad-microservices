package com.osucad.websocketgateway

import com.osucad.websocketgateway.config.Development
import com.osucad.websocketgateway.config.Production
import com.osucad.websocketgateway.plugins.configureHealthChecks
import com.osucad.websocketgateway.plugins.configureMetrics
import com.osucad.websocketgateway.plugins.configureRouting
import com.osucad.websocketgateway.plugins.configureSerialization
import com.osucad.websocketgateway.plugins.configureWebSockets
import com.osucad.websocketgateway.services.ServiceModule
import io.ktor.server.application.*
import io.ktor.server.netty.*
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.ksp.generated.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.development() {
    main(Development().module)
}

fun Application.production() {
    main(Production().module)
}

fun Application.main(module: Module) {
    install(Koin) {
        slf4jLogger()
        modules(
            module {
                single { environment }
            },
            ServiceModule().module,
            module,
        )
    }

    configureSerialization()
    configureMetrics()
    configureHealthChecks()
    configureWebSockets()
    configureRouting()
}
