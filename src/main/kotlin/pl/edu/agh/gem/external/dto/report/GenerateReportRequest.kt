package pl.edu.agh.gem.external.dto.report

import pl.edu.agh.gem.internal.model.report.ReportCreation
import pl.edu.agh.gem.internal.model.report.ReportFormat

data class GenerateReportRequest(
    val format: ReportFormat,
    val title: String,
)

fun GenerateReportRequest.toDomain(groupId: String, creatorId: String) =
    ReportCreation(
        format = format,
        title = title,
        creatorId = creatorId,
        groupId = groupId,
    )
