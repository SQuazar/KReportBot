package net.nullpointer.kreport.service

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.send
import net.dv8tion.jda.api.entities.ISnowflake
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.nullpointer.kreport.entity.Pageable
import net.nullpointer.kreport.entity.ReportData
import net.nullpointer.kreport.entity.ReportStatus
import net.nullpointer.kreport.repository.ReportDataRepository
import net.nullpointer.kreport.util.asEmbed
import net.nullpointer.kreport.util.toReportContext
import java.util.*

class ReportService(private val repository: ReportDataRepository) {

    suspend fun <T : ISnowflake> sendReport(
        reporter: User,
        target: User,
        ctx: T,
        reportChannel: TextChannel
    ): Message {
        val context = ctx.toReportContext()
        val report = ReportData(
            reportId = 0,
            authorId = reporter.idLong,
            targetId = target.idLong,
            context = context
        )
        val attachmentsText = if (ctx is Message && ctx.attachments.isNotEmpty()) {
            "Вложения:\n" + ctx.attachments.joinToString("\n") { it.url }
        } else null

        val message = reportChannel.send {
            content = attachmentsText
            allowedMentionTypes = EnumSet.noneOf(Message.MentionType::class.java)
            embeds += report.asEmbed(reporter.jda)
        }.await()
        repository.save(report.copy(reportId = message.idLong))

        return message
    }

    suspend fun getReportById(reportId: Long): ReportData? = repository.findById(reportId)

    suspend fun saveReport(report: ReportData) = repository.save(report)

    suspend fun deleteReport(reportId: Long) = repository.delete(reportId)

    suspend fun findReportsByTarget(targetId: Long): List<ReportData> = repository.findByTargetId(targetId)

    suspend fun findReportsByStatus(status: ReportStatus, page: Int = 0, limit: Int = 3): Pageable<ReportData> =
        repository.findReportsByStatus(status, page, limit)

}