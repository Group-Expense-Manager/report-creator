package pl.edu.agh.gem.external.dto

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import pl.edu.agh.gem.external.dto.user.toDomain
import pl.edu.agh.gem.util.createGroupUsersDetailsResponse

class GroupUsersDetailsResponseTest : ShouldSpec({

    should("map correctly to UsersDetails") {
        // given
        val groupUsersDetailsResponse = createGroupUsersDetailsResponse()

        // when
        val result = groupUsersDetailsResponse.toDomain()

        // then
        result.users.size shouldBe groupUsersDetailsResponse.details.size

        groupUsersDetailsResponse.details.forEach { userDetailsDto ->
            val userDetails = result.users[userDetailsDto.id]
            userDetails?.id shouldBe userDetailsDto.id
            userDetails?.username shouldBe userDetailsDto.username
            userDetails?.firstName shouldBe userDetailsDto.firstName
            userDetails?.lastName shouldBe userDetailsDto.lastName
        }
    }
},)
