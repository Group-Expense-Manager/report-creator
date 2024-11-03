package pl.edu.agh.gem.internal.job.stage

import io.kotest.core.spec.style.ShouldSpec
import org.bson.types.Binary
import org.mockito.Mockito.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import pl.edu.agh.gem.internal.job.ReportJobState.FETCHING_GROUP_DETAILS
import pl.edu.agh.gem.internal.job.ReportJobState.FETCHING_SETTLEMENTS
import pl.edu.agh.gem.internal.job.ReportJobState.FETCHING_USERS_DETAILS
import pl.edu.agh.gem.internal.job.ReportJobState.UPLOAD_REPORT
import pl.edu.agh.gem.internal.model.finance.GroupActivities
import pl.edu.agh.gem.internal.model.finance.GroupBalances
import pl.edu.agh.gem.internal.model.finance.GroupSettlements
import pl.edu.agh.gem.internal.model.group.GroupDetails
import pl.edu.agh.gem.internal.model.user.UsersDetails
import pl.edu.agh.gem.util.createReportJob

class GenerateReportStageTest : ShouldSpec({

    val generateReportStage = spy(
        object : GenerateReportStage() {
            override fun generateReport(
                balances: List<GroupBalances>,
                settlements: List<GroupSettlements>,
                activities: List<GroupActivities>,
                usersDetails: UsersDetails,
                groupDetails: GroupDetails,
            ): Binary {
                return Binary(ByteArray(0))
            }
        },
    )

    should("proceed to FETCHING_SETTLEMENTS state when balances, settlements, or activities are null") {
        // given
        val reportJob = createReportJob(balances = null)

        // when
        generateReportStage.process(reportJob)

        // then
        verify(generateReportStage).nextStage(reportJob, FETCHING_SETTLEMENTS)
    }

    should("proceed to FETCHING_USERS_DETAILS state when usersDetails is null") {
        // given
        val reportJob = createReportJob(usersDetails = null)

        // when
        generateReportStage.process(reportJob)

        // then
        verify(generateReportStage).nextStage(reportJob, FETCHING_USERS_DETAILS)
    }

    should("proceed to FETCHING_GROUP_DETAILS state when groupDetails is null") {
        // given
        val reportJob = createReportJob(groupDetails = null)

        // when
        generateReportStage.process(reportJob)

        // then
        verify(generateReportStage).nextStage(reportJob, FETCHING_GROUP_DETAILS)
    }

    should("generate report and proceed to UPLOAD_REPORT state when all data is available") {
        // given
        val reportJob = createReportJob()
        val reportFile = Binary(ByteArray(0))
        whenever(
            generateReportStage.generateReport(
                balances = reportJob.balances!!,
                settlements = reportJob.settlements!!,
                activities = reportJob.activities!!,
                usersDetails = reportJob.usersDetails!!,
                groupDetails = reportJob.groupDetails!!,
            ),
        ).thenReturn(reportFile)

        // when
        generateReportStage.process(reportJob)

        // then
        verify(generateReportStage).nextStage(reportJob.copy(file = reportFile), UPLOAD_REPORT)
    }
},)
