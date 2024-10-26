package pl.edu.agh.gem.internal.model.report

import java.time.Instant

data class ReportCreation(
        val title:String,
        val groupId:String,
        val format:String,
        val creatorId:String,
)
