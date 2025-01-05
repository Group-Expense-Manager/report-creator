package pl.edu.agh.gem.external.dto

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import pl.edu.agh.gem.external.dto.group.toDomain
import pl.edu.agh.gem.util.createUserGroupsResponse

class UserGroupsResponseTest : ShouldSpec({

    should("map correctly to Group") {
        // given
        val userGroupsResponse = createUserGroupsResponse()

        // when
        val result = userGroupsResponse.toDomain()

        // then
        result.size shouldBe userGroupsResponse.groups.size
        result.first().groupId shouldBe userGroupsResponse.groups.first().groupId
        result.last().groupId shouldBe userGroupsResponse.groups.last().groupId
    }
})
