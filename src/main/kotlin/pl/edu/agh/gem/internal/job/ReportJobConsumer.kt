package pl.edu.agh.gem.internal.job

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import pl.edu.agh.gem.internal.model.report.ReportJob
import java.util.concurrent.Executor

class ReportJobConsumer(
    private val reportJobFinder: ReportJobFinder,
    private val reportJobProcessor: ReportJobProcessor,
) {
    private var job: Job? = null

    fun consume(consumerExecutor: Executor) {
        job =
            CoroutineScope(consumerExecutor.asCoroutineDispatcher()).launch {
                reportJobFinder.findJobToProcess()
                    .collect {
                        launch {
                            processWithExceptionHandling(it)
                        }
                    }
            }
    }

    private fun processWithExceptionHandling(reportJob: ReportJob) {
        try {
            reportJobProcessor.processReportJob(reportJob)
        } catch (e: Exception) {
            log.error(e) { "Error while processing report job: $reportJob" }
        }
    }

    fun destroy() {
        job?.also {
            log.info { "Cancelling report job consumer job" }
            it.cancel()
        }
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}
