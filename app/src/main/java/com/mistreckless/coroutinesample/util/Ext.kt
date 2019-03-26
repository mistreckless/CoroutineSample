package com.mistreckless.coroutinesample.util

import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.selects.SelectBuilder
import kotlinx.coroutines.selects.select


suspend inline fun selectLoop(crossinline builder: SelectBuilder<Unit>.() -> Unit) {
    while (true) select(builder)
}

suspend inline fun <reified T> SendChannel<T>.trySend(msg: T) {
    try {
        send(msg)
    } catch (_: ClosedSendChannelException) {
    }
}
