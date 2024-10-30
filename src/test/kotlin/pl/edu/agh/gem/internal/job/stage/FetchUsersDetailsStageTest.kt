package pl.edu.agh.gem.internal.job.stage

import io.kotest.core.spec.style.ShouldSpec
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import pl.edu.agh.gem.internal.client.RetryableUserDetailsManagerClientException
import pl.edu.agh.gem.internal.client.UserDetailsManagerClient
import pl.edu.agh.gem.internal.job.ReportJobState.FETCHING_GROUP_DETAILS
import pl.edu.agh.gem.util.createReportJob
import pl.edu.agh.gem.util.createUsersDetails

class FetchUsersDetailsStageTest : ShouldSpec({

    val userDetailsManagerClient = mock(UserDetailsManagerClient::class.java)
    val fetchUsersDetailsStage = spy(FetchUsersDetailsStage(userDetailsManagerClient))

    should("fetch users details and proceed to FETCHING_GROUP_DETAILS state") {
        // given
        val reportJob = createReportJob()
        val usersDetails = createUsersDetails()
        whenever(userDetailsManagerClient.getUsersDetails(reportJob.groupId)).thenReturn(usersDetails)

        // when
        fetchUsersDetailsStage.process(reportJob)

        // then
        verify(fetchUsersDetailsStage).nextStage(reportJob.copy(usersDetails = usersDetails), FETCHING_GROUP_DETAILS)
        verify(userDetailsManagerClient).getUsersDetails(reportJob.groupId)
    }

    should("retry on RetryableUserDetailsManagerException") {
        // given
        val reportJob = createReportJob()
        whenever(userDetailsManagerClient.getUsersDetails(reportJob.groupId)).thenThrow(RetryableUserDetailsManagerClientException::class.java)

        // when
        fetchUsersDetailsStage.process(reportJob)

        // then
        verify(fetchUsersDetailsStage).retry()
        verify(userDetailsManagerClient).getUsersDetails(reportJob.groupId)
    }
},)
