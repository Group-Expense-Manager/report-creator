package pl.edu.agh.gem.internal.job

import mu.KotlinLogging
import pl.edu.agh.gem.internal.model.report.ReportJob

abstract class ProcessingStage {

    abstract fun process(reportJob: ReportJob): StageResult

    fun nextStage(reportJob: ReportJob, nextState: ReportJobState): StageResult {
        return NextStage(reportJob, nextState)
    }

    fun success(): StageResult {
        return StageSuccess
    }

    fun failure(exception: Exception): StageResult {
        return StageFailure(exception)
    }

    fun retry(): StageResult {
        return StageRetry
    }

    protected companion object {
        val logger = KotlinLogging.logger { }
    }
}

sealed class StageResult

data class NextStage(
    val reportJob: ReportJob,
    val newState: ReportJobState,
) : StageResult()

data object StageSuccess : StageResult()

data class StageFailure(
    val exception: Exception,
) : StageResult()

data object StageRetry : StageResult()
