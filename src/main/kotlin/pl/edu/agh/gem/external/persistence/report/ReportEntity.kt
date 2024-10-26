package pl.edu.agh.gem.external.persistence.report

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import pl.edu.agh.gem.internal.model.report.Report
import java.time.Instant

@Document("reports")
data class ReportEntity(
    @Id
    val id:String,
    val groupId:String,
    val format:String,
    val creatorId:String,
    val createdAt: Instant,
    val attachmentId: String
)

fun ReportEntity.toDomain(): Report {
    return Report(
        id = id,
        groupId = groupId,
        format = format,
        creatorId = creatorId,
        createdAt = createdAt,
        attachmentId = attachmentId
    )
}

fun Report.toEntity(): ReportEntity {
    return ReportEntity(
        id = id,
        groupId = groupId,
        format = format,
        creatorId = creatorId,
        createdAt = createdAt,
        attachmentId = attachmentId
    )
}
