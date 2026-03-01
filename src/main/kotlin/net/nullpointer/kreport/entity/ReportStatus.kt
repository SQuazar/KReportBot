package net.nullpointer.kreport.entity

import net.nullpointer.kreport.util.REPORT_ACCEPTED_COLOR
import net.nullpointer.kreport.util.REPORT_COLOR
import net.nullpointer.kreport.util.REPORT_DENIED_COLOR
import java.awt.Color

enum class ReportStatus(val locale: String, val color: Color) {
    PENDING("В ожидании", REPORT_COLOR),
    ACCEPTED("Принято", REPORT_ACCEPTED_COLOR),
    REJECTED("Отколнено", REPORT_DENIED_COLOR)
}