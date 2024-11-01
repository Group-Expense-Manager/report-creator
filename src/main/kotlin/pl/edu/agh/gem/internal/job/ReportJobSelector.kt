package pl.edu.agh.gem.internal.job

import org.springframework.stereotype.Service
import pl.edu.agh.gem.internal.job.ReportJobState.FETCHING_ACTIVITIES
import pl.edu.agh.gem.internal.job.ReportJobState.FETCHING_BALANCES
import pl.edu.agh.gem.internal.job.ReportJobState.FETCHING_GROUP_DETAILS
import pl.edu.agh.gem.internal.job.ReportJobState.FETCHING_SETTLEMENTS
import pl.edu.agh.gem.internal.job.ReportJobState.FETCHING_USERS_DETAILS
import pl.edu.agh.gem.internal.job.ReportJobState.FORMAT_SELECTION
import pl.edu.agh.gem.internal.job.ReportJobState.GENERATING_XLSX_REPORT
import pl.edu.agh.gem.internal.job.ReportJobState.NOTIFY
import pl.edu.agh.gem.internal.job.ReportJobState.SAVING
import pl.edu.agh.gem.internal.job.ReportJobState.STARTING
import pl.edu.agh.gem.internal.job.ReportJobState.UPLOAD_REPORT
import pl.edu.agh.gem.internal.job.stage.ErrorStage
import pl.edu.agh.gem.internal.job.stage.FetchActivitiesStage
import pl.edu.agh.gem.internal.job.stage.FetchBalanceStage
import pl.edu.agh.gem.internal.job.stage.FetchGroupDetailsStage
import pl.edu.agh.gem.internal.job.stage.FetchSettlementsStage
import pl.edu.agh.gem.internal.job.stage.FetchUsersDetailsStage
import pl.edu.agh.gem.internal.job.stage.FormatSelectionStage
import pl.edu.agh.gem.internal.job.stage.GenerateXLSXReportStage
import pl.edu.agh.gem.internal.job.stage.NotifyStage
import pl.edu.agh.gem.internal.job.stage.SavingStage
import pl.edu.agh.gem.internal.job.stage.StartingStage
import pl.edu.agh.gem.internal.job.stage.UploadReportStage

@Service
class ReportJobSelector(
    private val startingStage: StartingStage,
    private val fetchActivitiesStage: FetchActivitiesStage,
    private val fetchBalanceStage: FetchBalanceStage,
    private val fetchSettlementsStage: FetchSettlementsStage,
    private val fetchUsersDetailsStage: FetchUsersDetailsStage,
    private val fetchGroupDetailsStage: FetchGroupDetailsStage,
    private val formatSelectionStage: FormatSelectionStage,
    private val generateXLSXReportStage: GenerateXLSXReportStage,
    private val uploadReportStage: UploadReportStage,
    private val savingStage: SavingStage,
    private val notifyStage: NotifyStage,
    private val errorStage: ErrorStage,
) {
    fun select(state: ReportJobState): ProcessingStage {
        return when (state) {
            STARTING -> startingStage
            FETCHING_ACTIVITIES -> fetchActivitiesStage
            FETCHING_BALANCES -> fetchBalanceStage
            FETCHING_SETTLEMENTS -> fetchSettlementsStage
            FETCHING_USERS_DETAILS -> fetchUsersDetailsStage
            FETCHING_GROUP_DETAILS -> fetchGroupDetailsStage
            FORMAT_SELECTION -> formatSelectionStage
            GENERATING_XLSX_REPORT -> generateXLSXReportStage
            UPLOAD_REPORT -> uploadReportStage
            SAVING -> savingStage
            NOTIFY -> notifyStage
            else -> errorStage
        }
    }
}
