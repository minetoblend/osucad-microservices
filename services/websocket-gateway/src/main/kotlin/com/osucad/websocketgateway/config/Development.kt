package com.osucad.websocketgateway.config

import kotlinx.serialization.json.JsonBuilder
import org.koin.core.annotation.Module

@Module(includes = [])
class Development : ApplicationModule {
    override fun JsonBuilder.configureJson() {
        prettyPrint = true
    }
}
