package pl.edu.agh.gem.internal.job.stage

import org.bson.types.Binary
import pl.edu.agh.gem.internal.job.ProcessingStage
import pl.edu.agh.gem.internal.job.ReportJobState.FETCHING_GROUP_DETAILS
import pl.edu.agh.gem.internal.job.ReportJobState.FETCHING_SETTLEMENTS
import pl.edu.agh.gem.internal.job.ReportJobState.FETCHING_USERS_DETAILS
import pl.edu.agh.gem.internal.job.ReportJobState.SAVING
import pl.edu.agh.gem.internal.job.StageResult
import pl.edu.agh.gem.internal.model.finance.GroupActivities
import pl.edu.agh.gem.internal.model.finance.GroupBalances
import pl.edu.agh.gem.internal.model.finance.GroupSettlements
import pl.edu.agh.gem.internal.model.group.GroupDetails
import pl.edu.agh.gem.internal.model.report.ReportJob
import pl.edu.agh.gem.internal.model.user.UsersDetails

abstract class GenerateReportStage : ProcessingStage() {
    abstract fun generateReport(
        balances: List<GroupBalances>,
        settlements: List<GroupSettlements>,
        activities: List<GroupActivities>,
        usersDetails: UsersDetails,
        groupDetails: GroupDetails,
    ): Binary

    override fun process(reportJob: ReportJob): StageResult {
        return if (reportJob.balances == null || reportJob.settlements == null || reportJob.activities == null) {
            nextStage(reportJob, FETCHING_SETTLEMENTS)
        } else if (reportJob.usersDetails == null) {
            nextStage(reportJob, FETCHING_USERS_DETAILS)
        } else if (reportJob.groupDetails == null) {
            nextStage(reportJob, FETCHING_GROUP_DETAILS)
        } else {
            val reportFile = generateReport(
                balances = reportJob.balances,
                settlements = reportJob.settlements,
                activities = reportJob.activities,
                usersDetails = reportJob.usersDetails,
                groupDetails = reportJob.groupDetails,
            )

            nextStage(reportJob.copy(file = reportFile), SAVING)
        }
    }
}
