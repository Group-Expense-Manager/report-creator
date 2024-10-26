package pl.edu.agh.gem.external.dto.report

import pl.edu.agh.gem.internal.model.report.ReportCreation

data class GenerateReportRequest(
        val format:String,
        val title:String,
)

fun GenerateReportRequest.toReportCreation(groupId:String,creatorId:String) = 
        ReportCreation(
                format = format,
                title = title,
                creatorId = creatorId,
                groupId = groupId,
        )
