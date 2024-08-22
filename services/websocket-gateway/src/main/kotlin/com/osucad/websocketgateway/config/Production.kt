package com.osucad.websocketgateway.config

import com.osucad.websocketgateway.monitoring.DatadogMetricsModule
import org.koin.core.annotation.Module

@Module(includes = [DatadogMetricsModule::class])
class Production : ApplicationModule
