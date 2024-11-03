package pl.edu.agh.gem.integration.client

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import pl.edu.agh.gem.helper.group.DummyGroup.GROUP_ID
import pl.edu.agh.gem.integration.BaseIntegrationSpec
import pl.edu.agh.gem.integration.ability.stubGetUsersDetails
import pl.edu.agh.gem.internal.client.RetryableUserDetailsManagerClientException
import pl.edu.agh.gem.internal.client.UserDetailsManagerClient
import pl.edu.agh.gem.internal.client.UserDetailsManagerClientException
import pl.edu.agh.gem.util.createGroupUsersDetailsResponse

class UserDetailsManagerClientIT(
    private val userDetailsManagerClient: UserDetailsManagerClient,
) : BaseIntegrationSpec({
    should("get group users details") {
        // given
        val groupUsersDetails = createGroupUsersDetailsResponse()
        stubGetUsersDetails(body = groupUsersDetails, groupId = GROUP_ID)

        // when
        val result = userDetailsManagerClient.getUsersDetails(GROUP_ID)

        // then
        result.users.forEach { (key, value) ->
            value.id shouldBe key
            value.lastName shouldBe groupUsersDetails.details.find { it.id == key }?.lastName
            value.firstName shouldBe groupUsersDetails.details.find { it.id == key }?.firstName
            value.username shouldBe groupUsersDetails.details.find { it.id == key }?.username
        }
    }

    should("throw UserDetailsManagerClientException when we send bad request") {
        // given
        stubGetUsersDetails(groupId = GROUP_ID, statusCode = BAD_REQUEST)

        // when & then
        shouldThrow<UserDetailsManagerClientException> {
            userDetailsManagerClient.getUsersDetails(GROUP_ID)
        }
    }

    should("throw RetryableUserDetailsManagerClientException when client has internal error") {
        // given
        stubGetUsersDetails(groupId = GROUP_ID, statusCode = INTERNAL_SERVER_ERROR)

        // when & then
        shouldThrow<RetryableUserDetailsManagerClientException> {
            userDetailsManagerClient.getUsersDetails(GROUP_ID)
        }
    }
},)
