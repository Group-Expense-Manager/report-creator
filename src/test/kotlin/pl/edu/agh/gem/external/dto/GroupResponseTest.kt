package pl.edu.agh.gem.external.dto

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import pl.edu.agh.gem.external.dto.group.toDomain
import pl.edu.agh.gem.util.createGroupResponse

class GroupResponseTest : ShouldSpec({

    should("map correctly to GroupDetails") {
        // given
        val groupResponse = createGroupResponse()

        // when
        val result = groupResponse.toDomain()

        // then
        result.members.size shouldBe groupResponse.members.size
        result.members.first().id shouldBe groupResponse.members.first().id
        result.members.last().id shouldBe groupResponse.members.last().id
        result.groupCurrencies.size shouldBe groupResponse.groupCurrencies.size
        result.groupCurrencies.first().code shouldBe groupResponse.groupCurrencies.first().code
        result.groupCurrencies.last().code shouldBe groupResponse.groupCurrencies.last().code
        result.name shouldBe groupResponse.name
        result.attachmentId shouldBe groupResponse.attachmentId
    }
})
