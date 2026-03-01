package net.nullpointer.kreport.command

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.components.button
import dev.minn.jda.ktx.interactions.components.getOption
import dev.minn.jda.ktx.messages.MessageEdit
import kotlinx.datetime.toJavaInstant
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.components.actionrow.ActionRowChildComponent
import net.dv8tion.jda.api.components.buttons.ButtonStyle
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.internal.utils.Helpers
import net.nullpointer.kreport.entity.ReportStatus
import net.nullpointer.kreport.service.ReportService
import net.nullpointer.kreport.util.GUILD_ID
import net.nullpointer.kreport.util.REPORT_CHANNEL
import net.nullpointer.kreport.util.asEmbed
import net.nullpointer.kreport.util.sendEphemeralMessage
import kotlin.math.max

class SearchReportCommand(private val jda: JDA, name: String, private val reportService: ReportService) :
    GenericCommand<SlashCommandInteractionEvent>(
        jda,
        name,
        SlashCommandInteractionEvent::class.java,
    ) {
    override suspend fun onCommand(event: SlashCommandInteractionEvent): Boolean {
        val status = event.getOption<String>("status")?.let { ReportStatus.valueOf(it.uppercase()) }
            ?: return false
        val page = max((event.getOption<Int>("page") ?: 0) - 1, 0)

        event.deferReply(true).await()
        event.hook.sendEphemeralMessage("Получаю список жалоб...").await()

        createView(event, status, page)
        return true
    }

    private suspend fun createView(event: SlashCommandInteractionEvent, status: ReportStatus, page: Int) {
        val reports = reportService.findReportsByStatus(status, page)

        if (reports.items.isEmpty()) {
            event.hook.editOriginal("Ничего не найдено...").await()
            return
        }

        val reportEmbeds = reports.items.sortedBy { it.createdAt }
            .map {
                EmbedBuilder(it.asEmbed(jda))
                    .setTitle("Жалоба на пользователя")
                    .addField(
                        "Ссылка на жалобу",
                        Helpers.format(Message.JUMP_URL, GUILD_ID, REPORT_CHANNEL, it.reportId),
                        false
                    )
                    .setTimestamp(it.createdAt.toJavaInstant())
                    .build()
            }
        val message = MessageEdit {
            content = "Страница ${page + 1}/${reports.pageCount}"
            embeds += reportEmbeds
            val buttons = mutableListOf<ActionRowChildComponent>()
            if (reports.page > 0) {
                buttons += jda.button(label = "Назад", style = ButtonStyle.PRIMARY, user = event.user) {
                    it.deferEdit().await()
                    createView(event, status, page - 1)
                }
            }
            if (reports.hasNextPage) {
                buttons += jda.button(label = "Далее", style = ButtonStyle.PRIMARY, user = event.user) {
                    it.deferEdit().await()
                    createView(event, status, page + 1)
                }
            }

            if (buttons.isNotEmpty()) {
                actionRow {
                    components += buttons
                }
            }
        }

        event.hook.editOriginal(message).await()
    }
}