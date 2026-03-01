package net.nullpointer.kreport

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

suspend fun main() {
    ReportBot().start()
}

val scope = CoroutineScope(Dispatchers.Default)