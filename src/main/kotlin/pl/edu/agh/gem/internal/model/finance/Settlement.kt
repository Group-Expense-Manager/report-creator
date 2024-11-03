package pl.edu.agh.gem.internal.model.finance

import java.math.BigDecimal

data class GroupSettlements(
    val settlements: List<Settlement>,
    val currency: String,
    val status: SettlementStatus,
)

data class Settlement(
    val fromUserId: String,
    val toUserId: String,
    val value: BigDecimal,
)

enum class SettlementStatus {
    PENDING,
    SAVED,
}
