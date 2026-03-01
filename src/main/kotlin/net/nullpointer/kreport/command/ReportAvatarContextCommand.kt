package net.nullpointer.kreport.command

import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent
import net.nullpointer.kreport.service.ReportService
import net.nullpointer.kreport.util.REPORT_CHANNEL
import net.nullpointer.kreport.util.addReportReactions
import net.nullpointer.kreport.util.sendEphemeralMessage
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class ReportAvatarContextCommand(jda: JDA, name: String, private val reportService: ReportService) :
    GenericCommand<UserContextInteractionEvent>(
        jda,
        name,
        UserContextInteractionEvent::class.java,
    ) {
    override suspend fun onCommand(event: UserContextInteractionEvent): Boolean {
        event.deferReply(true).queue()
        val channel = event.guild?.getTextChannelById(REPORT_CHANNEL)
            ?: run {
                event.hook.sendEphemeralMessage("Канал для репортов не найден. Присосим свои извинения").queue()
                return false
            }

        val target = event.targetMember ?: run {
            event.hook.sendEphemeralMessage("Пользовать ${event.user.name} не является участником данного сервера!")
                .queue()
            return false
        }

        if (target.user.isBot) {
            event.hook.sendEphemeralMessage("Вы не можете пожаловаться на бота!").queue()
            return false
        }

        if (target.idLong == event.user.idLong) {
            event.hook.sendEphemeralMessage("Вы не можете отправить жалобу на себя!").queue()
            return false
        }

        reportService.sendReport(event.user, target.user, target, channel)
            .addReportReactions().await()
        event.hook.sendEphemeralMessage("Жалоба отправлена. Спасибо за обращение!").queue()

        return true
    }

    override fun getCooldownDuration(): Duration = 3.minutes
}