package com.osucad.protocol.operations

import kotlinx.serialization.Serializable

@Serializable
class UnorderedOperation(
    val clientId: String,
    val documentId: String,
    val deltas: List<Delta>
)
