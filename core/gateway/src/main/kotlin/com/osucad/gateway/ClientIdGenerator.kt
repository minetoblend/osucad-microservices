package com.osucad.gateway

import java.util.concurrent.atomic.AtomicLong

class ClientIdGenerator(private val shardId: Int) {
    private var nextId = AtomicLong(0)

    fun next(): String {
        val id = nextId.getAndIncrement().toString()

        return "$shardId:$id"
    }
}
