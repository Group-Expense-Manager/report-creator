package pl.edu.agh.gem.external.persistence.job

import org.bson.types.Binary
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import pl.edu.agh.gem.internal.job.ReportJobState
import pl.edu.agh.gem.internal.job.ReportJobState.STARTING
import pl.edu.agh.gem.internal.model.finance.Activity
import pl.edu.agh.gem.internal.model.finance.ActivityStatus
import pl.edu.agh.gem.internal.model.finance.ActivityType
import pl.edu.agh.gem.internal.model.finance.Balance
import pl.edu.agh.gem.internal.model.finance.GroupActivities
import pl.edu.agh.gem.internal.model.finance.GroupBalances
import pl.edu.agh.gem.internal.model.finance.GroupSettlements
import pl.edu.agh.gem.internal.model.finance.Settlement
import pl.edu.agh.gem.internal.model.finance.SettlementStatus
import pl.edu.agh.gem.internal.model.group.Currency
import pl.edu.agh.gem.internal.model.group.GroupDetails
import pl.edu.agh.gem.internal.model.report.ReportFormat
import pl.edu.agh.gem.internal.model.report.ReportJob
import pl.edu.agh.gem.internal.model.user.UserDetails
import pl.edu.agh.gem.internal.model.user.UsersDetails
import pl.edu.agh.gem.model.GroupMember
import java.math.BigDecimal
import java.time.Instant

@Document("jobs")
data class ReportJobEntity(
    @Id
    val id: String,
    val groupId: String,
    val title: String,
    val format: ReportFormat,
    val creatorId: String,
    val state: ReportJobState = STARTING,
    val balances: List<GroupBalancesEntity>?,
    val settlements: List<GroupSettlementsEntity>?,
    val activities: List<GroupActivitiesEntity>?,
    val usersDetails: UsersDetailsEntity?,
    val groupDetails: GroupDetailsEntity?,
    val file: Binary?,
    val attachmentId: String?,
    val nextProcessAt: Instant,
    val retry: Long = 0,
)

fun ReportJobEntity.toDomain(): ReportJob {
    return ReportJob(
        id = id,
        groupId = groupId,
        title = title,
        format = format,
        creatorId = creatorId,
        state = state,
        balances = balances?.map { it.toDomain() },
        settlements = settlements?.map { it.toDomain() },
        activities = activities?.map { it.toDomain() },
        usersDetails = usersDetails?.toDomain(),
        groupDetails = groupDetails?.toDomain(),
        file = file,
        attachmentId = attachmentId,
        nextProcessAt = nextProcessAt,
        retry = retry,
    )
}

fun ReportJob.toEntity(): ReportJobEntity {
    return ReportJobEntity(
        id = id,
        groupId = groupId,
        title = title,
        format = format,
        creatorId = creatorId,
        state = state,
        balances = balances?.map { it.toEntity() },
        settlements = settlements?.map { it.toEntity() },
        activities = activities?.map { it.toEntity() },
        usersDetails = usersDetails?.toEntity(),
        groupDetails = groupDetails?.toEntity(),
        file = file,
        attachmentId = attachmentId,
        nextProcessAt = nextProcessAt,
        retry = retry,
    )
}

data class UserDetailsEntity(
    val id: String,
    val username: String,
    val firstName: String?,
    val lastName: String?,
)

fun UserDetailsEntity.toDomain(): UserDetails {
    return UserDetails(
        id = id,
        username = username,
        firstName = firstName,
        lastName = lastName,
    )
}

fun UserDetails.toEntity(): UserDetailsEntity {
    return UserDetailsEntity(
        id = id,
        username = username,
        firstName = firstName,
        lastName = lastName,
    )
}

data class UsersDetailsEntity(
    val users: Map<String, UserDetailsEntity>,
)

fun UsersDetailsEntity.toDomain(): UsersDetails {
    return UsersDetails(
        users = users.mapValues { it.value.toDomain() },
    )
}

fun UsersDetails.toEntity(): UsersDetailsEntity {
    return UsersDetailsEntity(
        users = users.mapValues { it.value.toEntity() },
    )
}

data class GroupDetailsEntity(
    val members: List<GroupMemberEntity>,
    val groupCurrencies: List<CurrencyEntity>,
    val name: String,
    val attachmentId: String,
)

data class CurrencyEntity(
    val code: String,
)

fun CurrencyEntity.toDomain(): Currency {
    return Currency(
        code = code,
    )
}

fun Currency.toEntity(): CurrencyEntity {
    return CurrencyEntity(
        code = code,
    )
}

fun GroupDetailsEntity.toDomain(): GroupDetails {
    return GroupDetails(
        members = members.map { it.toDomain() },
        groupCurrencies = groupCurrencies.map { it.toDomain() },
        name = name,
        attachmentId = attachmentId,
    )
}

fun GroupDetails.toEntity(): GroupDetailsEntity {
    return GroupDetailsEntity(
        members = members.map { it.toEntity() },
        groupCurrencies = groupCurrencies.map { it.toEntity() },
        name = name,
        attachmentId = attachmentId,
    )
}

data class GroupMemberEntity(
    val id: String,
)

fun GroupMemberEntity.toDomain(): GroupMember {
    return GroupMember(
        id = id,
    )
}

fun GroupMember.toEntity(): GroupMemberEntity {
    return GroupMemberEntity(
        id = id,
    )
}

data class GroupBalancesEntity(
    val currency: String,
    val balances: List<BalanceEntity>,
)

fun GroupBalancesEntity.toDomain(): GroupBalances {
    return GroupBalances(
        currency = currency,
        balances = balances.map { it.toDomain() },
    )
}

fun GroupBalances.toEntity(): GroupBalancesEntity {
    return GroupBalancesEntity(
        currency = currency,
        balances = balances.map { it.toEntity() },
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

data class GroupSettlementsEntity(
    val currency: String,
    val status: SettlementStatus,
    val settlements: List<SettlementEntity>,
)

fun GroupSettlementsEntity.toDomain(): GroupSettlements {
    return GroupSettlements(
        currency = currency,
        status = status,
        settlements = settlements.map { it.toDomain() },
    )
}

fun GroupSettlements.toEntity(): GroupSettlementsEntity {
    return GroupSettlementsEntity(
        currency = currency,
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

data class GroupActivitiesEntity(
    val groupId: String,
    val activities: List<ActivityEntity>,
    val currency: String,
)

data class ActivityEntity(
    val activityId: String,
    val type: ActivityType,
    val creatorId: String,
    val title: String,
    val value: BigDecimal,
    val status: ActivityStatus,
    val participantIds: List<String>,
    val date: Instant,
)

fun GroupActivitiesEntity.toDomain(): GroupActivities {
    return GroupActivities(
        groupId = groupId,
        activities = activities.map { it.toDomain() },
        currency = currency,
    )
}

fun GroupActivities.toEntity(): GroupActivitiesEntity {
    return GroupActivitiesEntity(
        groupId = groupId,
        activities = activities.map { it.toEntity() },
        currency = currency,
    )
}

fun ActivityEntity.toDomain(): Activity {
    return Activity(
        id = activityId,
        type = type,
        creatorId = creatorId,
        title = title,
        value = value,
        status = status,
        participantIds = participantIds,
        date = date,
    )
}

fun Activity.toEntity(): ActivityEntity {
    return ActivityEntity(
        activityId = id,
        type = type,
        creatorId = creatorId,
        title = title,
        value = value,
        status = status,
        participantIds = participantIds,
        date = date,
    )
}
