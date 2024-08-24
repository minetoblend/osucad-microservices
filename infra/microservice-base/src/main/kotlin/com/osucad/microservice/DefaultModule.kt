package com.osucad.microservice

import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class DefaultModule {
    @Single
    fun getSerializer() = defaultSerializer()
}
