package pl.edu.agh.gem.util

import org.bson.types.Binary
import pl.edu.agh.gem.external.dto.finance.ActivitiesResponse
import pl.edu.agh.gem.external.dto.finance.ActivityDTO
import pl.edu.agh.gem.external.dto.finance.ActivityGroupDTO
import pl.edu.agh.gem.external.dto.finance.BalancesResponse
import pl.edu.agh.gem.external.dto.finance.GroupBalancesDto
import pl.edu.agh.gem.external.dto.finance.GroupSettlementsDto
import pl.edu.agh.gem.external.dto.finance.SettlementDto
import pl.edu.agh.gem.external.dto.finance.SettlementsResponse
import pl.edu.agh.gem.external.dto.finance.UserBalanceDto
import pl.edu.agh.gem.external.dto.group.CurrencyDto
import pl.edu.agh.gem.external.dto.group.GroupDTO
import pl.edu.agh.gem.external.dto.group.GroupResponse
import pl.edu.agh.gem.external.dto.group.MemberDto
import pl.edu.agh.gem.external.dto.group.UserGroupsResponse
import pl.edu.agh.gem.external.dto.report.GenerateReportRequest
import pl.edu.agh.gem.external.dto.report.ReportNotificationRequest
import pl.edu.agh.gem.external.dto.user.GroupUsersDetailsResponse
import pl.edu.agh.gem.external.dto.user.UserDetailsDto
import pl.edu.agh.gem.helper.group.DummyGroup.GROUP_ID
import pl.edu.agh.gem.helper.user.DummyUser.OTHER_USER_ID
import pl.edu.agh.gem.helper.user.DummyUser.USER_ID
import pl.edu.agh.gem.internal.job.ReportJobState
import pl.edu.agh.gem.internal.job.ReportJobState.STARTING
import pl.edu.agh.gem.internal.model.finance.Activity
import pl.edu.agh.gem.internal.model.finance.ActivityStatus
import pl.edu.agh.gem.internal.model.finance.ActivityStatus.ACCEPTED
import pl.edu.agh.gem.internal.model.finance.ActivityStatus.PENDING
import pl.edu.agh.gem.internal.model.finance.ActivityType
import pl.edu.agh.gem.internal.model.finance.ActivityType.EXPENSE
import pl.edu.agh.gem.internal.model.finance.Attachment
import pl.edu.agh.gem.internal.model.finance.Balance
import pl.edu.agh.gem.internal.model.finance.GroupActivities
import pl.edu.agh.gem.internal.model.finance.GroupBalances
import pl.edu.agh.gem.internal.model.finance.GroupSettlements
import pl.edu.agh.gem.internal.model.finance.Settlement
import pl.edu.agh.gem.internal.model.finance.SettlementStatus
import pl.edu.agh.gem.internal.model.finance.SettlementStatus.SAVED
import pl.edu.agh.gem.internal.model.group.Currency
import pl.edu.agh.gem.internal.model.group.GroupDetails
import pl.edu.agh.gem.internal.model.report.Report
import pl.edu.agh.gem.internal.model.report.ReportCreation
import pl.edu.agh.gem.internal.model.report.ReportFormat
import pl.edu.agh.gem.internal.model.report.ReportFormat.XLSX
import pl.edu.agh.gem.internal.model.report.ReportJob
import pl.edu.agh.gem.internal.model.user.UserDetails
import pl.edu.agh.gem.internal.model.user.UsersDetails
import pl.edu.agh.gem.model.GroupMember
import java.math.BigDecimal
import java.time.Instant

fun createActivitiesResponse(
    groupId: String = "groupId",
    currency: String = "USD",
    activityId: String = "activityId",
    type: ActivityType = EXPENSE,
    creatorId: String = USER_ID,
    title: String = "title",
    value: BigDecimal = BigDecimal("10.00"),
    status: ActivityStatus = ACCEPTED,
    participantIds: List<String> = listOf(USER_ID, OTHER_USER_ID),
    date: Instant = Instant.now(),
): ActivitiesResponse {
    val activityDTO =
        ActivityDTO(
            id = activityId,
            type = type,
            creatorId = creatorId,
            title = title,
            value = value,
            status = status,
            participantIds = participantIds,
            date = date,
        )
    return ActivitiesResponse(
        groupId = groupId,
        groupActivities = listOf(ActivityGroupDTO(currency, listOf(activityDTO))),
    )
}

fun createBalancesResponse(
    groupId: String = "groupId",
    currency: String = "USD",
    value: BigDecimal = BigDecimal("100.00"),
): BalancesResponse {
    val userBalanceDto1 =
        UserBalanceDto(
            userId = USER_ID,
            value = value,
        )
    val userBalanceDto2 =
        UserBalanceDto(
            userId = OTHER_USER_ID,
            value = value.negate(),
        )
    val groupBalancesDto =
        GroupBalancesDto(
            currency = currency,
            userBalances = listOf(userBalanceDto1, userBalanceDto2),
        )
    return BalancesResponse(
        groupId = groupId,
        balances = listOf(groupBalancesDto),
    )
}

fun createSettlementsResponse(
    groupId: String = "groupId",
    status: SettlementStatus = SAVED,
    currency: String = "USD",
    fromUserId: String = USER_ID,
    toUserId: String = OTHER_USER_ID,
    value: BigDecimal = BigDecimal("100.00"),
): SettlementsResponse {
    val settlementDto =
        SettlementDto(
            fromUserId = fromUserId,
            toUserId = toUserId,
            value = value,
        )
    val groupSettlementsDto =
        GroupSettlementsDto(
            status = status,
            currency = currency,
            settlements = listOf(settlementDto),
        )
    return SettlementsResponse(
        groupId = groupId,
        settlements = listOf(groupSettlementsDto),
    )
}

fun createGroupResponse(
    groupId: String = "groupId",
    members: List<MemberDto> = listOf(MemberDto(USER_ID), MemberDto(OTHER_USER_ID)),
    groupCurrencies: List<CurrencyDto> = listOf(CurrencyDto("USD"), CurrencyDto("EUR")),
    name: String = "Test Group",
    attachmentId: String = "attachment123",
): GroupResponse {
    return GroupResponse(
        groupId = groupId,
        members = members,
        groupCurrencies = groupCurrencies,
        name = name,
        attachmentId = attachmentId,
    )
}

fun createUserGroupsResponse(groups: List<GroupDTO> = listOf(GroupDTO("groupId"), GroupDTO("groupId1"))): UserGroupsResponse {
    return UserGroupsResponse(groups = groups)
}

fun createGenerateReportRequest(
    format: ReportFormat = XLSX,
    title: String = "Test Report",
): GenerateReportRequest {
    return GenerateReportRequest(
        format = format,
        title = title,
    )
}

fun createGroupUsersDetailsResponse(
    details: List<UserDetailsDto> =
        listOf(
            UserDetailsDto(USER_ID, "username1", "firstName1", "lastName1"),
            UserDetailsDto(OTHER_USER_ID, "username2", "firstName2", "lastName2"),
        ),
): GroupUsersDetailsResponse {
    return GroupUsersDetailsResponse(details = details)
}

fun createReportCreation(
    title: String = "title",
    groupId: String = "groupId",
    format: ReportFormat = XLSX,
    creatorId: String = USER_ID,
): ReportCreation {
    return ReportCreation(
        title = title,
        groupId = groupId,
        format = format,
        creatorId = creatorId,
    )
}

fun createReportJob(
    id: String = "reportId",
    groupId: String = "groupId",
    title: String = "title",
    format: ReportFormat = XLSX,
    creatorId: String = USER_ID,
    state: ReportJobState = STARTING,
    balances: List<GroupBalances>? = listOf(createGroupBalances()),
    settlements: List<GroupSettlements>? = listOf(createGroupSettlements()),
    activities: List<GroupActivities>? = listOf(createGroupActivities()),
    usersDetails: UsersDetails? = createUsersDetails(),
    groupDetails: GroupDetails? = createGroupDetails(),
    file: Binary? = Binary(ByteArray(0)),
    attachmentId: String? = "attachmentId",
    nextProcessAt: Instant = Instant.now(),
    retry: Long = 0,
): ReportJob {
    return ReportJob(
        id = id,
        groupId = groupId,
        title = title,
        format = format,
        creatorId = creatorId,
        state = state,
        balances = balances,
        settlements = settlements,
        activities = activities,
        usersDetails = usersDetails,
        groupDetails = groupDetails,
        file = file,
        attachmentId = attachmentId,
        nextProcessAt = nextProcessAt,
        retry = retry,
    )
}

fun createActivity(
    id: String = "activityId",
    type: ActivityType = EXPENSE,
    creatorId: String = USER_ID,
    title: String = "defaultTitle",
    value: BigDecimal = BigDecimal("100.00"),
    status: ActivityStatus = PENDING,
    participantIds: List<String> = listOf(USER_ID, OTHER_USER_ID),
    date: Instant = Instant.now(),
): Activity {
    return Activity(
        id = id,
        type = type,
        creatorId = creatorId,
        title = title,
        value = value,
        status = status,
        participantIds = participantIds,
        date = date,
    )
}

fun createGroupActivities(
    groupId: String = "groupId",
    activities: List<Activity> = listOf(createActivity()),
    currency: String = "USD",
): GroupActivities {
    return GroupActivities(
        groupId = groupId,
        activities = activities,
        currency = currency,
    )
}

fun createBalance(
    userId: String = "userId",
    value: BigDecimal = BigDecimal("100.00"),
): Balance {
    return Balance(
        userId = userId,
        value = value,
    )
}

fun createGroupBalances(
    balances: List<Balance> =
        listOf(
            createBalance(userId = USER_ID, value = BigDecimal("100.00")),
            createBalance(userId = OTHER_USER_ID, value = BigDecimal("-100.00")),
        ),
    currency: String = "USD",
): GroupBalances {
    return GroupBalances(
        balances = balances,
        currency = currency,
    )
}

fun createGroupDetails(
    groupId: String = "groupId",
    members: List<GroupMember> =
        listOf(
            GroupMember(USER_ID),
            GroupMember(OTHER_USER_ID),
        ),
    groupCurrencies: List<Currency> = listOf(Currency("USD")),
    name: String = "Test Group",
    attachmentId: String = "attachmentId",
): GroupDetails {
    return GroupDetails(
        groupId = groupId,
        members = members,
        groupCurrencies = groupCurrencies,
        name = name,
        attachmentId = attachmentId,
    )
}

fun createGroupSettlements(
    settlements: List<Settlement> =
        listOf(
            Settlement(
                fromUserId = USER_ID,
                toUserId = OTHER_USER_ID,
                value = BigDecimal("100.00"),
            ),
        ),
    currency: String = "USD",
    status: SettlementStatus = SAVED,
): GroupSettlements {
    return GroupSettlements(
        settlements = settlements,
        currency = currency,
        status = status,
    )
}

fun createUserDetails(
    id: String = USER_ID,
    username: String = "testuser",
    firstName: String? = "Test",
    lastName: String? = "User",
): UserDetails {
    return UserDetails(
        id = id,
        username = username,
        firstName = firstName,
        lastName = lastName,
    )
}

fun createUsersDetails(users: Map<String, UserDetails> = listOf(USER_ID, OTHER_USER_ID).associateWith { createUserDetails(id = it) }): UsersDetails {
    return UsersDetails(
        users = users,
    )
}

fun createAttachment(id: String = "attachmentId"): Attachment {
    return Attachment(
        id = id,
    )
}

fun createCurrenciesDto(vararg currency: String = arrayOf("USD", "EUR")) = currency.map { CurrencyDto(it) }

fun createMembersDto(vararg members: String = arrayOf(USER_ID, OTHER_USER_ID)) = members.map { MemberDto(it) }

fun createReportNotification(
    id: String = "id",
    attachmentId: String = "attachmentId",
    title: String = "title",
    userId: String = USER_ID,
    groupId: String = GROUP_ID,
) = ReportNotificationRequest(
    id = id,
    title = title,
    creatorId = userId,
    attachmentId = attachmentId,
    groupId = groupId,
)

fun createReport(
    id: String = "id",
    groupId: String = "groupId",
    format: ReportFormat = XLSX,
    creatorId: String = USER_ID,
    createdAt: Instant = Instant.now(),
    attachmentId: String = "attachmentId",
) = Report(
    id = id,
    groupId = groupId,
    format = format,
    creatorId = creatorId,
    createdAt = createdAt,
    attachmentId = attachmentId,
)
