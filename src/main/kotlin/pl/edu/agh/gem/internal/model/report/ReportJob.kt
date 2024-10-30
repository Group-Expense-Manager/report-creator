package pl.edu.agh.gem.internal.model.report

import org.bson.types.Binary
import pl.edu.agh.gem.internal.job.ReportJobState
import pl.edu.agh.gem.internal.job.ReportJobState.STARTING
import pl.edu.agh.gem.internal.model.finance.GroupActivities
import pl.edu.agh.gem.internal.model.finance.GroupBalances
import pl.edu.agh.gem.internal.model.finance.GroupSettlements
import pl.edu.agh.gem.internal.model.group.GroupDetails
import pl.edu.agh.gem.internal.model.user.UsersDetails
import java.time.Clock
import java.time.Instant
import java.util.UUID.randomUUID

data class ReportJob(
    val id: String = randomUUID().toString(),
    val groupId: String,
    val title: String,
    val format: ReportFormat,
    val creatorId: String,
    val state: ReportJobState = STARTING,
    val balances: List<GroupBalances>? = null,
    val settlements: List<GroupSettlements>? = null,
    val activities: List<GroupActivities>? = null,
    val usersDetails: UsersDetails? = null,
    val groupDetails: GroupDetails? = null,
    val file: Binary? = null,
    val attachmentId: String? = null,
    val nextProcessAt: Instant,
    val retry: Long = 0,
)

fun ReportCreation.toReportJob(clock: Clock) = ReportJob(
    groupId = groupId,
    title = title,
    creatorId = creatorId,
    format = format,
    nextProcessAt = clock.instant(),
)
