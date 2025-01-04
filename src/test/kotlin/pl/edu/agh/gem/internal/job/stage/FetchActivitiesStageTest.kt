package pl.edu.agh.gem.internal.job.stage

import io.kotest.core.spec.style.ShouldSpec
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import pl.edu.agh.gem.internal.client.FinanceAdapterClient
import pl.edu.agh.gem.internal.client.RetryableFinanceAdapterClientException
import pl.edu.agh.gem.internal.job.ReportJobState.FETCHING_SETTLEMENTS
import pl.edu.agh.gem.internal.job.ReportJobState.FETCHING_USERS_DETAILS
import pl.edu.agh.gem.util.createGroupActivities
import pl.edu.agh.gem.util.createReportJob

class FetchActivitiesStageTest : ShouldSpec({

    val financeAdapterClient = mock<FinanceAdapterClient>()
    val fetchActivitiesStage = spy(FetchActivitiesStage(financeAdapterClient))

    should("fetch activities and proceed to FETCHING_USERS_DETAILS state") {
        // given
        val reportJob = createReportJob()
        val activities = listOf(createGroupActivities())
        whenever(financeAdapterClient.getActivities(reportJob.groupId)).thenReturn(activities)

        // when
        fetchActivitiesStage.process(reportJob)

        // then
        verify(fetchActivitiesStage).nextStage(reportJob.copy(activities = activities), FETCHING_USERS_DETAILS)
        verify(financeAdapterClient).getActivities(reportJob.groupId)
    }

    should("proceed to FETCHING_SETTLEMENTS state on RetryableFinanceAdapterClientException") {
        // given
        val reportJob = createReportJob()
        whenever(financeAdapterClient.getActivities(reportJob.groupId)).thenThrow(RetryableFinanceAdapterClientException::class.java)

        // when
        fetchActivitiesStage.process(reportJob)

        // then
        verify(fetchActivitiesStage).nextStage(reportJob, FETCHING_SETTLEMENTS)
        verify(financeAdapterClient).getActivities(reportJob.groupId)
    }
})
