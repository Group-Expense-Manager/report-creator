package pl.edu.agh.gem.internal.job

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import pl.edu.agh.gem.internal.model.report.ReportJob
import pl.edu.agh.gem.internal.persistence.ReportJobRepository

@Service
class ReportJobProcessor(
    private val reportJobSelector: ReportJobSelector,
    private val reportJobRepository: ReportJobRepository,
) {
    fun processReportJob(reportJob: ReportJob) {
        when (val nextState = reportJobSelector.select(reportJob.state).process(reportJob)) {
            is NextStage -> handleNextStage(nextState)
            is StageSuccess -> handleStateSuccess(reportJob)
            is StageFailure -> handleStateFailure(reportJob)
                .also {
                    log.error(nextState.exception) { "Failure occurred on job $reportJob" }
                }
            is StageRetry -> handleStateRetry(reportJob)
        }
    }

    private fun handleStateSuccess(reportJob: ReportJob) {
        log.info {
            "Success on job with groupId ${reportJob.groupId} and creator: ${reportJob.creatorId}"
        }
        reportJobRepository.remove(reportJob)
    }

    private fun handleStateFailure(reportJob: ReportJob) {
        log.error { "Failure occurred on job with groupId ${reportJob.groupId} and creator: ${reportJob.creatorId}" }
        reportJobRepository.remove(reportJob)
    }

    private fun handleStateRetry(reportJob: ReportJob) {
        log.warn { "Retry for on job with groupId ${reportJob.groupId} and creator: ${reportJob.creatorId}" }
        reportJobRepository.updateNextProcessAtAndRetry(reportJob)
    }

    private fun handleNextStage(nextStage: NextStage) {
        reportJobRepository.save(
            nextStage.reportJob.copy(
                state = nextStage.newState,
            ),
        )
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}
