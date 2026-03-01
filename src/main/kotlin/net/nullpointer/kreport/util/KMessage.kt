package net.nullpointer.kreport.util

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.requests.RestAction

fun Message.addReportReactions() = RestAction.allOf(
    addReaction(ACCEPT_EMOJI),
    addReaction(DENIED_EMOJI),
    addReaction(DELETE_EMOJI)
)