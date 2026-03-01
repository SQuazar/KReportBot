package net.nullpointer.kreport.registry

import dev.minn.jda.ktx.interactions.commands.*
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.interactions.commands.Command
import net.nullpointer.kreport.command.ReportAvatarContextCommand
import net.nullpointer.kreport.command.ReportContextCommand
import net.nullpointer.kreport.command.SearchReportCommand
import net.nullpointer.kreport.command.UserReportStatsCommand
import net.nullpointer.kreport.entity.ReportStatus
import net.nullpointer.kreport.service.ReportService
import net.nullpointer.kreport.util.contextCommand

class CommandRegistry(
    private val jda: JDA,
    private val reportService: ReportService
) {
    fun registerCommands() {
        jda.updateCommands {
            contextCommand(Command.Type.MESSAGE, "Пожаловаться") {
                setContexts(InteractionContextType.GUILD)
                setNameLocalizations(report)
                ReportContextCommand(jda, name, reportService)
            }
            contextCommand(Command.Type.USER, "Пожаловаться на аватар") {
                setContexts(InteractionContextType.GUILD)
                setNameLocalizations(reportAvatar)
                ReportAvatarContextCommand(jda, name, reportService)
            }
            slash("stats", "Статистика пользователя") {
                restrict(guild = true)
                option<User>("user", "Пользователь", true)
                UserReportStatsCommand(jda, name, reportService)
            }
            slash("reports", "Поиск жалоб") {
                restrict(guild = true, Permission.MODERATE_MEMBERS)
                option<String>("status", "Статус жалобы", true) {
                    ReportStatus.entries.forEach { status ->
                        choice(status.locale, status.name)
                    }
                }
                option<Int>("page", "Страница", false) {
                    setMinValue(0)
                }

                SearchReportCommand(jda, name, reportService)
            }
        }.queue()
    }

    private val report = mapOf(
        DiscordLocale.RUSSIAN to "Пожаловаться",
        DiscordLocale.ENGLISH_UK to "Report",
        DiscordLocale.ENGLISH_US to "Report",
    )

    private val reportAvatar = mapOf(
        DiscordLocale.RUSSIAN to "Пожаловаться на аватар",
        DiscordLocale.ENGLISH_UK to "Report Avatar",
        DiscordLocale.ENGLISH_US to "Report Avatar",
    )
}