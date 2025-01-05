package pl.edu.agh.gem.internal.job.stage

import io.kotest.core.spec.style.ShouldSpec
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import pl.edu.agh.gem.internal.client.GroupManagerClient
import pl.edu.agh.gem.internal.client.RetryableUserDetailsManagerClientException
import pl.edu.agh.gem.internal.job.ReportJobState.FORMAT_SELECTION
import pl.edu.agh.gem.util.createGroupDetails
import pl.edu.agh.gem.util.createReportJob

class FetchGroupDetailsStageTest : ShouldSpec({

    val groupManagerClient = mock(GroupManagerClient::class.java)
    val fetchGroupDetailsStage = spy(FetchGroupDetailsStage(groupManagerClient))

    should("fetch group details and proceed to FORMAT_SELECTION state") {
        // given
        val reportJob = createReportJob()
        val groupDetails = createGroupDetails()
        whenever(groupManagerClient.getGroupDetails(reportJob.groupId)).thenReturn(groupDetails)

        // when
        fetchGroupDetailsStage.process(reportJob)

        // then
        verify(fetchGroupDetailsStage).nextStage(reportJob.copy(groupDetails = groupDetails), FORMAT_SELECTION)
        verify(groupManagerClient).getGroupDetails(reportJob.groupId)
    }

    should("retry on RetryableUserDetailsManagerException") {
        // given
        val reportJob = createReportJob()
        whenever(groupManagerClient.getGroupDetails(reportJob.groupId)).thenThrow(RetryableUserDetailsManagerClientException::class.java)

        // when
        fetchGroupDetailsStage.process(reportJob)

        // then
        verify(fetchGroupDetailsStage).retry()
        verify(groupManagerClient).getGroupDetails(reportJob.groupId)
    }
})
