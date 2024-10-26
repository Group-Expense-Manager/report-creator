package pl.edu.agh.gem.external.persistence.job

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import pl.edu.agh.gem.internal.job.ReportJobState
import pl.edu.agh.gem.internal.job.ReportJobState.STARTING
import pl.edu.agh.gem.internal.model.finance.Activity
import pl.edu.agh.gem.internal.model.finance.ActivityStatus
import pl.edu.agh.gem.internal.model.finance.ActivityType
import pl.edu.agh.gem.internal.model.finance.Balance
import pl.edu.agh.gem.internal.model.finance.Balances
import pl.edu.agh.gem.internal.model.finance.Settlement
import pl.edu.agh.gem.internal.model.finance.SettlementStatus
import pl.edu.agh.gem.internal.model.finance.Settlements
import pl.edu.agh.gem.internal.model.report.ReportJob
import java.math.BigDecimal
import java.time.Instant

@Document("jobs")
data class ReportJobEntity(
    @Id
    val id: String,
    val groupId: String,
    val title: String,
    val creatorId: String,
    val state: ReportJobState = STARTING,
    val balances: List<BalancesEntity>?,
    val settlements: List<SettlementsEntity>?,
    val activities: List<ActivityEntity>?,
    val nextProcessAt: Instant,
    val retry: Long = 0,
)

fun ReportJobEntity.toDomain(): ReportJob {
    return ReportJob(
            id = id,
            groupId = groupId,
            title = title,
            creatorId = creatorId,
            state = state,
            balances = balances?.map { it.toDomain() },
            settlements = settlements?.map { it.toDomain() },
            activities = activities?.map { it.toDomain() },
            nextProcessAt = nextProcessAt,
            retry = retry,
    )
}

fun ReportJob.toEntity(): ReportJobEntity {
    return ReportJobEntity(
            id = id,
            groupId = groupId,
            title = title,
            creatorId = creatorId,
            state = state,
            balances = balances?.map { it.toEntity() },
            settlements = settlements?.map { it.toEntity() },
            activities = activities?.map { it.toEntity() },
            nextProcessAt = nextProcessAt,
            retry = retry,
    )
}

data class BalancesEntity(
    val id: BalancesCompositeKey,
    val balances: List<BalanceEntity>,
)

data class BalancesCompositeKey(
    val groupId: String,
    val currency: String,
)

fun BalancesEntity.toDomain(): Balances {
    return Balances(
            groupId = id.groupId,
            currency = id.currency,
            users = balances.map { it.toDomain() },
    )
}

fun Balances.toEntity(): BalancesEntity {
    return BalancesEntity(
            id = BalancesCompositeKey(
                    groupId = groupId,
                    currency = currency,
            ),
            balances = users.map { it.toEntity() },
    )
}

data class BalanceEntity(
    val userId: String,
    val balance: BigDecimal,
)

fun BalanceEntity.toDomain(): Balance {
    return Balance(
            userId = userId,
            value = balance,
    )
}

fun Balance.toEntity(): BalanceEntity {
    return BalanceEntity(
            userId = userId,
            balance = value,
    )
}

data class SettlementsEntity(
    val id: SettlementsCompositeKey,
    val status: SettlementStatus,
    val settlements: List<SettlementEntity>,
)

data class SettlementsCompositeKey(
    val groupId: String,
    val currency: String,
)

fun SettlementsEntity.toDomain(): Settlements {
    return Settlements(
            groupId = id.groupId,
            currency = id.currency,
            status = status,
            settlements = settlements.map { it.toDomain() },
    )
}

fun Settlements.toEntity(): SettlementsEntity {
    return SettlementsEntity(
            id = SettlementsCompositeKey(
                    groupId = groupId,
                    currency = currency,
            ),
            status = status,
            settlements = settlements.map { it.toEntity() },
    )
}

data class SettlementEntity(
    val fromUserId: String,
    val toUserId: String,
    val value: BigDecimal,
)

fun SettlementEntity.toDomain(): Settlement {
    return Settlement(
            fromUserId = fromUserId,
            toUserId = toUserId,
            value = value,
    )
}

fun Settlement.toEntity(): SettlementEntity {
    return SettlementEntity(
            fromUserId = fromUserId,
            toUserId = toUserId,
            value = value,
    )
}

data class ActivityEntity(
    val activityId: String,
    val type: ActivityType,
    val creatorId: String,
    val title: String,
    val value: BigDecimal,
    val currency: String,
    val status: ActivityStatus,
    val participantIds: List<String>,
    val date: Instant,
)

fun ActivityEntity.toDomain(): Activity {
    return Activity(
            activityId = activityId,
            type = type,
            creatorId = creatorId,
            title = title,
            value = value,
            currency = currency,
            status = status,
            participantIds = participantIds,
            date = date,
    )
}

fun Activity.toEntity(): ActivityEntity {
    return ActivityEntity(
            activityId = activityId,
            type = type,
            creatorId = creatorId,
            title = title,
            value = value,
            currency = currency,
            status = status,
            participantIds = participantIds,
            date = date,
    )
}
