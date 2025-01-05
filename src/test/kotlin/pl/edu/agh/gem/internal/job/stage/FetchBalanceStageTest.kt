package pl.edu.agh.gem.internal.job.stage

import io.kotest.core.spec.style.ShouldSpec
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import pl.edu.agh.gem.internal.client.FinanceAdapterClient
import pl.edu.agh.gem.internal.client.RetryableFinanceAdapterClientException
import pl.edu.agh.gem.internal.job.ReportJobState.FETCHING_ACTIVITIES
import pl.edu.agh.gem.internal.job.ReportJobState.FETCHING_SETTLEMENTS
import pl.edu.agh.gem.util.createGroupBalances
import pl.edu.agh.gem.util.createReportJob

class FetchBalanceStageTest : ShouldSpec({

    val financeAdapterClient = mock(FinanceAdapterClient::class.java)
    val fetchBalanceStage = spy(FetchBalanceStage(financeAdapterClient))

    should("fetch balances and proceed to FETCHING_ACTIVITIES state") {
        // given
        val reportJob = createReportJob()
        val balances = listOf(createGroupBalances())
        whenever(financeAdapterClient.getBalances(reportJob.groupId)).thenReturn(balances)

        // when
        fetchBalanceStage.process(reportJob)

        // then
        verify(fetchBalanceStage).nextStage(reportJob.copy(balances = balances), FETCHING_ACTIVITIES)
        verify(financeAdapterClient).getBalances(reportJob.groupId)
    }

    should("proceed to FETCHING_SETTLEMENTS state on RetryableFinanceAdapterClientException") {
        // given
        val reportJob = createReportJob()
        whenever(financeAdapterClient.getBalances(reportJob.groupId)).thenThrow(RetryableFinanceAdapterClientException::class.java)

        // when
        fetchBalanceStage.process(reportJob)

        // then
        verify(fetchBalanceStage).nextStage(reportJob, FETCHING_SETTLEMENTS)
        verify(financeAdapterClient).getBalances(reportJob.groupId)
    }
})
