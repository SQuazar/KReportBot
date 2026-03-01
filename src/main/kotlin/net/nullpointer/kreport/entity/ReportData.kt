package net.nullpointer.kreport.entity

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class ReportData(
    val reportId: Long,
    val authorId: Long,
    val targetId: Long,
    var moderatorId: Long? = null,
    var status: ReportStatus = ReportStatus.PENDING,
    val context: ReportContext,

    @Contextual
    val createdAt: Instant = Clock.System.now(),
    @Contextual
    val updatedAt: Instant = Clock.System.now()
)
