package pl.edu.agh.gem.external.dto.report

data class ReportNotificationRequest(
    val id: String,
    val title: String,
    val userId: String,
    val attachmentId: String,
)
