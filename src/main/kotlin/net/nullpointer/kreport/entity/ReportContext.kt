package net.nullpointer.kreport.entity

import kotlinx.serialization.Serializable

@Serializable
sealed interface ReportContext

@Serializable
data class MessageContext(
    val message: String,
    val messageId: Long,
    val channelId: Long,
    val guildId: Long,
    val attachments: List<String> = listOf()
) : ReportContext

@Serializable
data class AvatarContext(
    val avatarUrl: String
) : ReportContext