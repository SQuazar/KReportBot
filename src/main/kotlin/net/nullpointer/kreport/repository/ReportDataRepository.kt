package net.nullpointer.kreport.repository

import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.client.model.Sorts
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import net.nullpointer.kreport.entity.Pageable
import net.nullpointer.kreport.entity.ReportData
import net.nullpointer.kreport.entity.ReportStatus

class ReportDataRepository(private val database: MongoDatabase) {
    companion object {
        private const val COLLECTION_NAME = "DATA_reports"
    }

    suspend fun findById(id: Long): ReportData? =
        database.getCollection<ReportData>(COLLECTION_NAME)
            .find<ReportData>(Filters.eq(ReportData::reportId.name, id))
            .firstOrNull()

    suspend fun findByTargetId(targetId: Long): List<ReportData> =
        database.getCollection<ReportData>(COLLECTION_NAME)
            .find<ReportData>(Filters.eq(ReportData::targetId.name, targetId))
            .toList()

    suspend fun findReportsByStatus(status: ReportStatus, page: Int, limit: Int): Pageable<ReportData> {
        val collection = database.getCollection<ReportData>(COLLECTION_NAME)
        val total = collection.countDocuments(Filters.eq(ReportData::status.name, status))
        val items = collection.find<ReportData>(Filters.eq(ReportData::status.name, status.name))
            .sort(Sorts.descending(ReportData::createdAt.name))
            .skip(page * limit)
            .limit(limit)
            .toList()

        val pageCount = if (total == 0L) 1 else ((total + limit - 1) / limit).toInt()
        val hasNext = page + 1 < pageCount

        return Pageable(items, page, pageCount, hasNext)
    }

    suspend fun save(reportData: ReportData) =
        database.getCollection<ReportData>(COLLECTION_NAME)
            .replaceOne(
                Filters.eq(ReportData::reportId.name, reportData.reportId),
                reportData,
                ReplaceOptions().upsert(true)
            )

    suspend fun delete(id: Long) =
        database.getCollection<ReportData>(COLLECTION_NAME)
            .deleteMany(Filters.eq(ReportData::reportId.name, id))
}