package pl.edu.agh.gem.external.dto

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import pl.edu.agh.gem.external.dto.finance.toDomain
import pl.edu.agh.gem.util.createBalancesResponse

class BalancesResponseTest : ShouldSpec({

    should("map correctly to GroupBalances") {
        // given
        val balancesResponse = createBalancesResponse()

        // when
        val result = balancesResponse.toDomain()

        // then
        result.size shouldBe 1
        result.first().also {
            val groupBalancesDto = balancesResponse.groupBalances.first()
            it.currency shouldBe groupBalancesDto.currency
            it.balances.size shouldBe 2
            it.balances.find { it.userId == groupBalancesDto.userBalances.first().userId }.also { balance ->
                balance?.value shouldBe groupBalancesDto.userBalances.first().value
            }
            it.balances.find { it.userId == groupBalancesDto.userBalances.last().userId }.also { balance ->
                balance?.value shouldBe groupBalancesDto.userBalances.last().value
            }
        }
    }
},)
