package net.nullpointer.kreport.handler

import net.dv8tion.jda.api.events.GenericEvent

interface GenericEventHandler<T : GenericEvent> {
    suspend fun handle(event: T)
}