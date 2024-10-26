package pl.edu.agh.gem.internal.job

import org.springframework.stereotype.Service
import pl.edu.agh.gem.internal.job.ReportJobState.SAVING
import pl.edu.agh.gem.internal.job.ReportJobState.STARTING
import pl.edu.agh.gem.internal.job.stage.ErrorStage
import pl.edu.agh.gem.internal.job.stage.SavingStage
import pl.edu.agh.gem.internal.job.stage.StartingStage

@Service
class ReportJobSelector(
    private val startingStage: StartingStage,
    private val savingStage: SavingStage,
    private val errorStage: ErrorStage,
) {
    fun select(state: ReportJobState): ProcessingStage {
        return when (state) {
            STARTING -> startingStage
            SAVING -> savingStage
            else -> errorStage
        }
    }
}
