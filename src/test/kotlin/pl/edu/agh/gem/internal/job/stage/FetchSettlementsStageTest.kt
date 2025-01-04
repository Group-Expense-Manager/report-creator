package pl.edu.agh.gem.internal.job.stage

import io.kotest.core.spec.style.ShouldSpec
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import pl.edu.agh.gem.internal.client.FinanceAdapterClient
import pl.edu.agh.gem.internal.client.RetryableFinanceAdapterClientException
import pl.edu.agh.gem.internal.job.ReportJobState.FETCHING_BALANCES
import pl.edu.agh.gem.internal.model.finance.SettlementStatus.PENDING
import pl.edu.agh.gem.internal.model.finance.SettlementStatus.SAVED
import pl.edu.agh.gem.util.createGroupSettlements
import pl.edu.agh.gem.util.createReportJob

class FetchSettlementsStageTest : ShouldSpec({

    val financeAdapterClient = mock(FinanceAdapterClient::class.java)
    val fetchSettlementsStage = spy(FetchSettlementsStage(financeAdapterClient))

    should("retry if any settlement has PENDING status") {
        // given
        val reportJob = createReportJob()
        val settlements = listOf(createGroupSettlements(status = PENDING))
        whenever(financeAdapterClient.getSettlements(reportJob.groupId)).thenReturn(settlements)

        // when
        fetchSettlementsStage.process(reportJob)

        // then
        verify(fetchSettlementsStage).retry()
        verify(financeAdapterClient).getSettlements(reportJob.groupId)
    }

    should("proceed to FETCHING_BALANCES state if no settlement has PENDING status") {
        // given
        val reportJob = createReportJob()
        val settlements = listOf(createGroupSettlements(status = SAVED))
        whenever(financeAdapterClient.getSettlements(reportJob.groupId)).thenReturn(settlements)

        // when
        fetchSettlementsStage.process(reportJob)

        // then
        verify(fetchSettlementsStage).nextStage(reportJob.copy(settlements = settlements), FETCHING_BALANCES)
        verify(financeAdapterClient).getSettlements(reportJob.groupId)
    }

    should("retry on RetryableFinanceAdapterClientException") {
        // given
        val reportJob = createReportJob()
        whenever(financeAdapterClient.getSettlements(reportJob.groupId)).thenThrow(RetryableFinanceAdapterClientException::class.java)

        // when
        fetchSettlementsStage.process(reportJob)

        // then
        verify(fetchSettlementsStage).retry()
        verify(financeAdapterClient).getSettlements(reportJob.groupId)
    }
})
