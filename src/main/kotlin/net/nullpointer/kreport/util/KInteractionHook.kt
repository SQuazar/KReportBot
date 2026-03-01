package net.nullpointer.kreport.util

import net.dv8tion.jda.api.interactions.InteractionHook

fun InteractionHook.sendEphemeralMessage(message: String) = setEphemeral(true).sendMessage(message)