package pl.edu.agh.gem.internal.client

import pl.edu.agh.gem.internal.model.finance.Activity
import pl.edu.agh.gem.internal.model.finance.Balances
import pl.edu.agh.gem.internal.model.finance.Settlements

interface FinanceAdapterClient {
    fun getActivities(groupId:String): List<Activity>
    fun getBalances(groupId: String): List<Balances>
    fun getSettlements(groupId: String): List<Settlements>
}

class FinanceAdapterClientException(override val message: String?) : RuntimeException()

class RetryableFinanceAdapterClientException(override val message: String?) : RuntimeException()
