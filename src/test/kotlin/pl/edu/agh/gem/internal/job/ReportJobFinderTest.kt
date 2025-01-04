package pl.edu.agh.gem.internal.job

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.take
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import pl.edu.agh.gem.config.ReportJobProcessorProperties
import pl.edu.agh.gem.internal.persistence.ReportJobRepository
import pl.edu.agh.gem.util.TestThreadExecutor
import pl.edu.agh.gem.util.createReportJob

class ReportJobFinderTest : ShouldSpec({
    val reportJobRepository = mock<ReportJobRepository>()
    val reportJobProperties = mock<ReportJobProcessorProperties>()
    val reportJobFinder =
        ReportJobFinder(
            TestThreadExecutor(),
            reportJobRepository,
            reportJobProperties,
        )

    should("emit report job") {
        // given
        val reportJob = createReportJob()
        whenever(reportJobRepository.findJobToProcessAndLock()).thenReturn(reportJob)

        // when
        val result = reportJobFinder.findJobToProcess()

        // then
        result.take(1).collect {
            it shouldBe reportJob
        }
    }
})
