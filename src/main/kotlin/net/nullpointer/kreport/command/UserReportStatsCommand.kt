package net.nullpointer.kreport.command

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.components.getOption
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.send
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.nullpointer.kreport.entity.ReportStatus
import net.nullpointer.kreport.service.ReportService
import net.nullpointer.kreport.util.REPORT_COLOR
import net.nullpointer.kreport.util.sendEphemeralMessage
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class UserReportStatsCommand(jda: JDA, name: String, private val reportService: ReportService) :
    GenericCommand<SlashCommandInteractionEvent>(jda, name, SlashCommandInteractionEvent::class.java) {
    override suspend fun onCommand(event: SlashCommandInteractionEvent): Boolean {
        event.getOption<User>("user")?.let { user ->
            event.deferReply(true).await()

            val reports = reportService.findReportsByTarget(user.idLong)
            val embed = Embed {
                title = "Статистика пользователя"
                color = REPORT_COLOR.rgb
                author {
                    name = user.effectiveName
                    iconUrl = user.effectiveAvatarUrl
                }
                field("Получено жалоб", reports.count().toString(), false)
                field("Одобрено жалоб", reports.count { it.status == ReportStatus.ACCEPTED }.toString(), true)
                field("Отклонено жалоб", reports.count { it.status == ReportStatus.REJECTED }.toString(), true)
            }

            if (reports.isNotEmpty()) {
                event.hook.send {
                    embeds += embed
                }.await()
            } else event.hook.sendEphemeralMessage("Жалобы на пользователя не найдены").await()
        }
        return true
    }

    override fun getCooldownDuration(): Duration = 1.minutes
}