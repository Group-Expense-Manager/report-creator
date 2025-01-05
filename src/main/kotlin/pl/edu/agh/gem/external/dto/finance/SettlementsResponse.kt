package pl.edu.agh.gem.external.dto.finance

import pl.edu.agh.gem.internal.model.finance.GroupSettlements
import pl.edu.agh.gem.internal.model.finance.Settlement
import pl.edu.agh.gem.internal.model.finance.SettlementStatus
import java.math.BigDecimal

data class SettlementsResponse(
    val groupId: String,
    val settlements: List<GroupSettlementsDto>,
)

data class GroupSettlementsDto(
    val status: SettlementStatus,
    val currency: String,
    val settlements: List<SettlementDto>,
)

data class SettlementDto(
    val fromUserId: String,
    val toUserId: String,
    val value: BigDecimal,
)

fun SettlementsResponse.toDomain() =
    settlements.map {
        GroupSettlements(
            currency = it.currency,
            status = it.status,
            settlements =
                it.settlements.map { settlement ->
                    Settlement(
                        fromUserId = settlement.fromUserId,
                        toUserId = settlement.toUserId,
                        value = settlement.value,
                    )
                },
        )
    }
