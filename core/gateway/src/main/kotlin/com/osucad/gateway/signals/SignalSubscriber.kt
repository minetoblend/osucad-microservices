package com.osucad.gateway.signals

import com.osucad.protocol.SignalWithSender

interface SignalSubscriber {
    suspend fun subscribe(block: suspend (signal: SignalWithSender) -> Unit)

    fun startTrackingDocument(documentId: String)

    fun stopTrackingDocument(documentId: String)

}
