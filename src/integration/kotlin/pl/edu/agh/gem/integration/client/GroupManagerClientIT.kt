package pl.edu.agh.gem.integration.client

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.NOT_ACCEPTABLE
import pl.edu.agh.gem.external.dto.group.GroupDTO
import pl.edu.agh.gem.helper.group.DummyGroup.GROUP_ID
import pl.edu.agh.gem.helper.group.DummyGroup.OTHER_GROUP_ID
import pl.edu.agh.gem.helper.user.DummyUser.OTHER_USER_ID
import pl.edu.agh.gem.helper.user.DummyUser.USER_ID
import pl.edu.agh.gem.integration.BaseIntegrationSpec
import pl.edu.agh.gem.integration.ability.stubGroupManagerGroupDetails
import pl.edu.agh.gem.integration.ability.stubGroupManagerUserGroups
import pl.edu.agh.gem.internal.client.GroupManagerClient
import pl.edu.agh.gem.internal.client.GroupManagerClientException
import pl.edu.agh.gem.internal.client.RetryableGroupManagerClientException
import pl.edu.agh.gem.internal.model.group.Currency
import pl.edu.agh.gem.model.GroupMember
import pl.edu.agh.gem.util.createCurrenciesDto
import pl.edu.agh.gem.util.createGroupResponse
import pl.edu.agh.gem.util.createMembersDto
import pl.edu.agh.gem.util.createUserGroupsResponse

class GroupManagerClientIT(
    private val groupManagerClient: GroupManagerClient,
) : BaseIntegrationSpec({
    should("get group") {
        // given
        val members = createMembersDto(USER_ID, OTHER_USER_ID)
        val listOfCurrencies = createCurrenciesDto("PLN", "USD", "EUR")
        val groupResponse = createGroupResponse(members = members, groupCurrencies = listOfCurrencies)
        stubGroupManagerGroupDetails(groupResponse, GROUP_ID)

        // when
        val result = groupManagerClient.getGroupDetails(GROUP_ID)

        // then
        result.also {
            it.shouldNotBeNull()
            it.groupCurrencies shouldBe listOfCurrencies.map { currency -> Currency(currency.code) }
            it.members shouldBe members.map { member -> GroupMember(member.id) }
        }
    }

    should("throw GroupManagerClientException when we send bad request") {
        // given
        val groupResponse = createGroupResponse()
        stubGroupManagerGroupDetails(groupResponse, GROUP_ID, NOT_ACCEPTABLE)

        // when & then
        shouldThrow<GroupManagerClientException> {
            groupManagerClient.getGroupDetails(GROUP_ID)
        }
    }

    should("throw RetryableCurrencyManagerClientException when client has internal error") {
        // given
        val groupResponse = createGroupResponse()
        stubGroupManagerGroupDetails(groupResponse, GROUP_ID, INTERNAL_SERVER_ERROR)

        // when & then
        shouldThrow<RetryableGroupManagerClientException> {
            groupManagerClient.getGroupDetails(GROUP_ID)
        }
    }

    should("get user groups") {
        // given
        val userGroups = listOf(GroupDTO(GROUP_ID), GroupDTO(OTHER_GROUP_ID))
        val userGroupsResponse = createUserGroupsResponse(userGroups)
        stubGroupManagerUserGroups(userGroupsResponse, USER_ID)

        // when
        val result = groupManagerClient.getGroups(USER_ID)

        // then
        result.forEach {
            it.groupId shouldBeIn listOf(GROUP_ID, OTHER_GROUP_ID)
        }
    }

    should("throw GroupManagerClientException when we send bad request") {
        // given
        stubGroupManagerUserGroups(createUserGroupsResponse(), USER_ID, NOT_ACCEPTABLE)

        // when & then
        shouldThrow<GroupManagerClientException> {
            groupManagerClient.getGroups(USER_ID)
        }
    }

    should("throw RetryableGroupManagerClientException when client has internal error") {
        // given
        stubGroupManagerUserGroups(createUserGroupsResponse(), USER_ID, INTERNAL_SERVER_ERROR)

        // when & then
        shouldThrow<RetryableGroupManagerClientException> {
            groupManagerClient.getGroups(USER_ID)
        }
    }
},)
