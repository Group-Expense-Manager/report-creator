package pl.edu.agh.gem.internal.client

import pl.edu.agh.gem.internal.model.finance.GroupActivities
import pl.edu.agh.gem.internal.model.finance.GroupBalances
import pl.edu.agh.gem.internal.model.finance.GroupSettlements

interface FinanceAdapterClient {
    fun getActivities(groupId: String): List<GroupActivities>

    fun getBalances(groupId: String): List<GroupBalances>

    fun getSettlements(groupId: String): List<GroupSettlements>
}

class FinanceAdapterClientException(override val message: String?) : RuntimeException()

class RetryableFinanceAdapterClientException(override val message: String?) : RuntimeException()
