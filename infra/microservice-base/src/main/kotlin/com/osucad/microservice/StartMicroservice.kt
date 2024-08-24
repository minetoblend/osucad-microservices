package com.osucad.microservice

import io.ktor.server.application.*
import org.koin.ktor.ext.getKoin

fun Application.startMicroservice(init: Application.() -> Unit = {}) {
    getKoin().declare(environment)

    configureSerialization()

    for (plugin in getKoin().getAll<ApplicationPlugin<Unit>>()) {
        install(plugin)
    }

    init()
}
