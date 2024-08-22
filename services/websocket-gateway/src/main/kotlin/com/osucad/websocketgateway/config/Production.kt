package com.osucad.websocketgateway.config

import com.osucad.websocketgateway.monitoring.DatadogModule
import org.koin.core.annotation.Module

@Module(includes = [DatadogModule::class])
class Production : ApplicationModule
