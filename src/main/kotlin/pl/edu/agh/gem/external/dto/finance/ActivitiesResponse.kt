package pl.edu.agh.gem.external.dto.finance

import pl.edu.agh.gem.internal.model.finance.Activity
import pl.edu.agh.gem.internal.model.finance.ActivityStatus
import pl.edu.agh.gem.internal.model.finance.ActivityType
import pl.edu.agh.gem.internal.model.finance.GroupActivities
import java.math.BigDecimal
import java.time.Instant

data class ActivitiesResponse(
    val groupId: String,
    val groupActivities: List<ActivityGroupDTO>,
)

data class ActivityGroupDTO(
    val currency: String,
    val activities: List<ActivityDTO>,
)

data class ActivityDTO(
    val id: String,
    val type: ActivityType,
    val creatorId: String,
    val title: String,
    val value: BigDecimal,
    val status: ActivityStatus,
    val participantIds: List<String>,
    val date: Instant,
)

fun ActivitiesResponse.toDomain() =
    groupActivities.map { activity ->
        GroupActivities(
            groupId = groupId,
            activities = activity.activities.map {
                Activity(
                    id = it.id,
                    type = it.type,
                    creatorId = it.creatorId,
                    title = it.title,
                    value = it.value,
                    status = it.status,
                    participantIds = it.participantIds,
                    date = it.date,
                )
            },
            currency = activity.currency,
        )
    }
