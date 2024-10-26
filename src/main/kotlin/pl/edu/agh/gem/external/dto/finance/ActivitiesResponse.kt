package pl.edu.agh.gem.external.dto.finance

import pl.edu.agh.gem.internal.model.finance.Activity
import pl.edu.agh.gem.internal.model.finance.ActivityStatus
import pl.edu.agh.gem.internal.model.finance.ActivityType
import java.math.BigDecimal
import java.time.Instant

data class ActivitiesResponse(
    val groupId: String,
    val activities: List<ActivityDTO>,
)

data class ActivityDTO(
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

fun ActivitiesResponse.toDomain() =
        this.activities.map { 
            Activity(
                    activityId = it.activityId,
                    type = it.type,
                    creatorId = it.creatorId,
                    title = it.title,
                    value = it.value,
                    currency = it.currency,
                    status = it.status,
                    participantIds = it.participantIds,
                    date = it.date
            )
        }
