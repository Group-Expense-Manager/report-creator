package pl.edu.agh.gem.external.dto.report

data class ReportNotificationRequest(
    val id: String,
    val groupId: String,
    val title: String,
    val creatorId: String,
    val attachmentId: String,
)
