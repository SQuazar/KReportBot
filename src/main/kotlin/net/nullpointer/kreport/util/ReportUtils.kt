package net.nullpointer.kreport.util

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.internal.utils.Helpers
import net.nullpointer.kreport.entity.AvatarContext
import net.nullpointer.kreport.entity.MessageContext
import net.nullpointer.kreport.entity.ReportContext
import net.nullpointer.kreport.entity.ReportData

suspend fun ReportData.asEmbed(jda: JDA): MessageEmbed {
    return createReportEmbed(
        this,
        jda.retrieveUserById(authorId).await(),
        jda.retrieveUserById(targetId).await(),
        context
    )
}

inline fun <reified T : ReportContext> ReportData.getContext(): T? {
    return context as? T
}

private fun createReportEmbed(
    report: ReportData,
    reporter: User,
    target: User,
    context: ReportContext
): MessageEmbed {
    val builder = EmbedBuilder {
        title = when (context) {
            is AvatarContext -> "Новая жалоба на аватар"
            is MessageContext -> "Новая жалоба"
        }
        author {
            name = "${reporter.name} | ${reporter.idLong}"
            iconUrl = reporter.effectiveAvatarUrl
        }
        field("Нарушитель", target.asMention, false)
        if (report.moderatorId != null) field("Модератор", "<@${report.moderatorId}>", false)
        color = report.status.color.rgb
    }

    return when(context) {
        is AvatarContext -> {
            builder.image = context.avatarUrl
            builder.build()
        }
        is MessageContext -> {
            if (context.message.isNotBlank()) {
                builder.field {
                    name = "Сообщение"
                    value =
                        if (context.message.length > 1024) context.message.take(1024) else context.message
                    inline = false
                }
            }
            builder.field("Ссылка на сообщение",
                Helpers.format(Message.JUMP_URL, context.guildId, context.channelId, context.messageId))
            builder.build()
        }
    }
}