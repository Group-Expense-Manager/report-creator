package pl.edu.agh.gem.internal.model.report

import org.bson.types.Binary
import pl.edu.agh.gem.internal.job.ReportJobState
import pl.edu.agh.gem.internal.job.ReportJobState.STARTING
import pl.edu.agh.gem.internal.model.finance.Activity
import pl.edu.agh.gem.internal.model.finance.Balances
import pl.edu.agh.gem.internal.model.finance.Settlements
import java.time.Clock
import java.time.Instant
import java.time.Instant.now
import java.util.UUID.randomUUID

data class ReportJob(
    val id: String = randomUUID().toString(),
    val groupId: String,
    val title: String,
    val creatorId: String,
    val state: ReportJobState = STARTING,
    val balances: List<Balances>? = null,
    val settlements: List<Settlements>? = null,
    val activities: List<Activity>? = null,
    val file: Binary? = null,
    val nextProcessAt: Instant,
    val retry: Long = 0,
)

fun ReportCreation.toReportJob(clock: Clock) = ReportJob(
        groupId = groupId,
        title = title,
        creatorId = creatorId,
        nextProcessAt = now(clock)
)
