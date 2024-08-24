package com.osucad.common

interface MessageSubscriber<T> {
    suspend fun subscribe(handler: suspend (T) -> Unit)
}
