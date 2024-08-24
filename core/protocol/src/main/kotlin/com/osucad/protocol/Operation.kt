package com.osucad.protocol

import com.osucad.protocol.operations.Delta
import com.osucad.protocol.operations.OrderedDeltas
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("submit_op")
class SubmitOperation(
    val documentId: String,
    val deltas: List<Delta>
) : ClientMessage

@Serializable
@SerialName("op")
class OrderedOperation(
    val documentId: String,
    val deltas: List<OrderedDeltas>
) : ServerMessage
