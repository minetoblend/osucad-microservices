package com.osucad.protocol.operations

import kotlinx.serialization.Serializable

@Serializable
class Delta(
    val content: String,
    val clientSequenceNumber: Long,
)
