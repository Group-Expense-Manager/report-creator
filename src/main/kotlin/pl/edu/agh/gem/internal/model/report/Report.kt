package pl.edu.agh.gem.internal.model.report

import java.time.Instant

data class Report(
    val id: String,
    val groupId: String,
    val format: ReportFormat,
    val creatorId: String,
    val createdAt: Instant,
    val attachmentId: String,
)

enum class ReportFormat {
    XLSX,
}

fun ReportJob.toReport(attachmentId: String) =
    Report(
        id = id,
        groupId = groupId,
        format = format,
        creatorId = creatorId,
        createdAt = nextProcessAt,
        attachmentId = attachmentId,
    )
