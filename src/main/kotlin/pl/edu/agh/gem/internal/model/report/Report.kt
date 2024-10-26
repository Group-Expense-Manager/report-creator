package pl.edu.agh.gem.internal.model.report

import java.time.Instant

data class Report(
        val id:String,
        val groupId:String,
        val format:String,
        val creatorId:String,
        val createdAt: Instant,
        val attachmentId: String
)
