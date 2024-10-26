package pl.edu.agh.gem.internal.model.finance

import java.math.BigDecimal

data class Balances(
    val users: List<Balance>,
    val currency: String,
)
data class Balance(
    val userId: String,
    val value: BigDecimal,
)
