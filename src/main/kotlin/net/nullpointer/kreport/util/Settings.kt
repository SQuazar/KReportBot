package net.nullpointer.kreport.util

val GUILD_ID: Long = System.getenv("GUILD_ID")?.toLongOrNull() ?: error("GUILD_ID is required")
val REPORT_CHANNEL: Long = System.getenv("REPORT_CHANNEL")?.toLongOrNull() ?: error("REPORT_CHANNEL is required")
val MODERATOR_ROLE: Long = System.getenv("MODERATOR_ROLE")?.toLongOrNull() ?: error("MODERATOR_ROLE_ID is required")