package com.osucad.protocol.operations

import kotlinx.serialization.Serializable

@Serializable
class OrderedDeltas(
    val sequenceNumber: Long,
    val clientSequenceNumber: Long,
    val clientId: String,
    val content: String,
)
