package pl.edu.agh.gem.internal.job

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.time.delay
import io.github.oshai.kotlinlogging.KotlinLogging
import pl.edu.agh.gem.config.ReportJobProcessorProperties
import pl.edu.agh.gem.internal.model.report.ReportJob
import pl.edu.agh.gem.internal.persistence.ReportJobRepository
import java.util.concurrent.Executor

class ReportJobFinder(
    private val producerExecutor: Executor,
    private val reportJobRepository: ReportJobRepository,
    private val reportJobProcessorProperties: ReportJobProcessorProperties,
) {
    fun findJobToProcess() = flow {
        while (currentCoroutineContext().isActive) {
            val reportJob = findReportJob()
            reportJob?.let {
                emit(it)
                log.info { "Emitted report Job : $it" }
            }
            waitOnEmpty(reportJob)
        }
    }.flowOn(producerExecutor.asCoroutineDispatcher())

    private fun findReportJob(): ReportJob? {
        try {
            return reportJobRepository.findJobToProcessAndLock()
        } catch (e: Exception) {
            log.error(e) { "Error while finding report job to process" }
            return null
        }
    }

    private suspend fun waitOnEmpty(reportJob: ReportJob?) {
        if (reportJob == null) {
            log.info { "No report job to process. Waiting for new job" }
            delay(reportJobProcessorProperties.emptyCandidateDelay)
        }
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}
