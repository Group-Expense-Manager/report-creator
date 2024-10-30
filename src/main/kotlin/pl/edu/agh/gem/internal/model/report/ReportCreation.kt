package pl.edu.agh.gem.internal.model.report

data class ReportCreation(
    val title: String,
    val groupId: String,
    val format: ReportFormat,
    val creatorId: String,
)
