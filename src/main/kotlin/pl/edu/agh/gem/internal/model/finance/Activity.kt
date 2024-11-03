package pl.edu.agh.gem.internal.model.finance

import java.math.BigDecimal
import java.time.Instant

data class GroupActivities(
    val groupId: String,
    val activities: List<Activity>,
    val currency: String,
)

data class Activity(
    val id: String,
    val type: ActivityType,
    val creatorId: String,
    val title: String,
    val value: BigDecimal,
    val status: ActivityStatus,
    val participantIds: List<String>,
    val date: Instant,
)

enum class ActivityType {
    EXPENSE,
    PAYMENT,
}

enum class ActivityStatus {
    ACCEPTED,
    REJECTED,
    PENDING,
}
