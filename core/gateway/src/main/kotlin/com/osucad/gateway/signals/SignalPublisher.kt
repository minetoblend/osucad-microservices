package com.osucad.gateway.signals

import com.osucad.protocol.Signal
import com.osucad.protocol.SignalWithSender

interface SignalPublisher {
    suspend fun publish(signal: SignalWithSender)

    suspend fun publish(
        documentId: String,
        clientId: String?,
        signal: Signal,
        targetClientId: String? = null,
    ) = publish(
        SignalWithSender(
            documentId = documentId,
            clientId = clientId,
            signal = signal,
            targetClientId = targetClientId,
        )
    )
}
