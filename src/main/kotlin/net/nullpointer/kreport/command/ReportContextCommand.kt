package net.nullpointer.kreport.command

import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
import net.nullpointer.kreport.service.ReportService
import net.nullpointer.kreport.util.REPORT_CHANNEL
import net.nullpointer.kreport.util.addReportReactions
import net.nullpointer.kreport.util.sendEphemeralMessage
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class ReportContextCommand(jda: JDA, name: String, val reportService: ReportService) : GenericCommand<MessageContextInteractionEvent>(
    jda, name, MessageContextInteractionEvent::class.java
) {
    override suspend fun onCommand(event: MessageContextInteractionEvent): Boolean {
        event.deferReply(true).queue()

        val channel = event.guild?.getTextChannelById(REPORT_CHANNEL)
            ?: run {
                event.hook.sendEphemeralMessage("Канал для репортов не найден. Присосим свои извинения").queue()
                return false
            }

        val message = event.target

        if (message.author.isBot) {
            event.hook.sendEphemeralMessage("Вы не можете пожаловаться на бота!").queue()
            return false
        }

        if (message.author.idLong == event.user.idLong) {
            event.hook.sendEphemeralMessage("Вы не можете пожаловаться на себя!").queue()
            return false
        }

        reportService.sendReport(event.user, message.author, message, channel)
            .addReportReactions().await()
        event.hook.sendEphemeralMessage("Жалоба отправлена. Спасибо за обращение!").queue()

        return true
    }

    override fun getCooldownDuration(): Duration = 3.minutes
}