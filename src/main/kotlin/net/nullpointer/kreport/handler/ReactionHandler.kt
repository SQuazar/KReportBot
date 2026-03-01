package net.nullpointer.kreport.handler

import dev.minn.jda.ktx.coroutines.await
import kotlinx.datetime.Clock
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.requests.RestAction
import net.nullpointer.kreport.entity.ReportStatus
import net.nullpointer.kreport.service.ReportService
import net.nullpointer.kreport.util.ACCEPT_EMOJI
import net.nullpointer.kreport.util.DENIED_EMOJI
import net.nullpointer.kreport.util.GUILD_ID
import net.nullpointer.kreport.util.MODERATOR_ROLE

class ReactionHandler(val reportService: ReportService) : GenericEventHandler<MessageReactionAddEvent> {

    override suspend fun handle(event: MessageReactionAddEvent) {
        if (!event.isFromGuild || event.guild.idLong != GUILD_ID) return
        if (event.user?.isBot == true) return
        if (event.member?.roles?.none { it.idLong == MODERATOR_ROLE } ?: true) return

        val message = event.retrieveMessage().await()
        if (message.author.idLong != event.jda.selfUser.idLong) return
        if (message.embeds.isEmpty()) return

        val report = reportService.getReportById(message.idLong) ?: return

        if (report.status != ReportStatus.PENDING) return

        val embed = message.embeds.first()

        val status = when (event.emoji.asCustom().idLong) {
            ACCEPT_EMOJI.idLong -> ReportStatus.ACCEPTED
            DENIED_EMOJI.idLong -> ReportStatus.REJECTED
            else -> null
        }

        if (status == null) {
            message.delete().queue()
            reportService.deleteReport(report.reportId)
            return
        }
        RestAction.allOf(
            message.clearReactions(),
            message.editMessageEmbeds(
                EmbedBuilder(embed).apply {
                    setColor(status.color)
                    addField("Модератор", event.user?.asMention ?: "unknown", false)
                }.build()
            )
        ).await()

        report.status = status
        report.moderatorId = event.user?.idLong
        report.updatedAt = Clock.System.now()
        reportService.saveReport(report)
    }
}