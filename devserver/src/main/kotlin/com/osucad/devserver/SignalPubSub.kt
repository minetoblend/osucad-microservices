package com.osucad.devserver

import com.osucad.gateway.signals.SignalPublisher
import com.osucad.gateway.signals.SignalSubscriber
import com.osucad.protocol.SignalWithSender
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class SignalPubSub : SignalPublisher, SignalSubscriber {

    private val signalSendFlow = MutableSharedFlow<SignalWithSender>()
    private val signalFlow = signalSendFlow.asSharedFlow()

    override suspend fun publish(signal: SignalWithSender) {
        signalSendFlow.emit(signal)
    }

    override suspend fun subscribe(block: suspend (signal: SignalWithSender) -> Unit) {
        signalFlow.collect(block)
    }

    override fun startTrackingDocument(documentId: String) {
        // no-op
    }

    override fun stopTrackingDocument(documentId: String) {
        // no-op
    }
}
