package com.osucad.common

import kotlinx.coroutines.delay
import kotlin.time.Duration

suspend fun pollForever(
    pollInterval: Duration,
    block: suspend () -> Boolean
) {
    while (true) {
        val result = block()

        if (!result)
            delay(pollInterval)
    }
}
