package pl.edu.agh.gem.external.dto

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import pl.edu.agh.gem.external.dto.finance.toDomain
import pl.edu.agh.gem.util.createActivitiesResponse

class ActivitiesResponseTest : ShouldSpec(
    {
        should("map correctly to GroupActivities") {
            // given
            val activitiesResponse = createActivitiesResponse()

            // when
            val result = activitiesResponse.toDomain()

            // then
            result.size shouldBe 1
            result.first().also {
                it.groupId shouldBe activitiesResponse.groupId
                it.currency shouldBe activitiesResponse.groupActivities.first().currency
                it.activities.size shouldBe 1
                it.activities.first().also { activity ->
                    val activityDTO = activitiesResponse.groupActivities.first().activities.first()
                    activity.id shouldBe activityDTO.id
                    activity.type shouldBe activityDTO.type
                    activity.creatorId shouldBe activityDTO.creatorId
                    activity.title shouldBe activityDTO.title
                    activity.value shouldBe activityDTO.value
                    activity.status shouldBe activityDTO.status
                    activity.participantIds shouldBe activityDTO.participantIds
                    activity.date shouldBe activityDTO.date
                }
            }
        }
    },
)
