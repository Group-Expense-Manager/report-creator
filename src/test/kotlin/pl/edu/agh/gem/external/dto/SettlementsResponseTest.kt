package pl.edu.agh.gem.external.dto

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import pl.edu.agh.gem.external.dto.finance.toDomain
import pl.edu.agh.gem.util.createSettlementsResponse

class SettlementsResponseTest : ShouldSpec({

    should("map correctly to GroupSettlements") {
        // given
        val settlementsResponse = createSettlementsResponse()

        // when
        val result = settlementsResponse.toDomain()

        // then
        result.size shouldBe 1
        result.first().also {
            val groupSettlementsDto = settlementsResponse.settlements.first()
            it.currency shouldBe groupSettlementsDto.currency
            it.status shouldBe groupSettlementsDto.status
            it.settlements.size shouldBe 1
            it.settlements.first().also { settlement ->
                val settlementDto = groupSettlementsDto.settlements.first()
                settlement.fromUserId shouldBe settlementDto.fromUserId
                settlement.toUserId shouldBe settlementDto.toUserId
                settlement.value shouldBe settlementDto.value
            }
        }
    }
},)
