package com.osucad.common

interface MessagePublisher<T> {
    suspend fun publish(message: T)
}
