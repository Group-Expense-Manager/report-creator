package pl.edu.agh.gem.external.dto.finance

import pl.edu.agh.gem.internal.model.finance.Balance
import pl.edu.agh.gem.internal.model.finance.Balances
import java.math.BigDecimal

data class BalancesResponse(
    val groupId: String,
    val groupBalances: List<GroupBalancesDto>,
)

data class GroupBalancesDto(
    val currency: String,
    val userBalances: List<UserBalanceDto>,
)

data class UserBalanceDto(
    val userId: String,
    val value: BigDecimal,
)

fun BalancesResponse.toDomain() =
        groupBalances.map { 
            Balances(
                    currency = it.currency,
                    users = it.userBalances.map { user -> Balance(userId = user.userId, value = user.value) }
            )
        }

