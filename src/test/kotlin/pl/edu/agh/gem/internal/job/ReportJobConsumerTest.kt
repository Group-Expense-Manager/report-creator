package pl.edu.agh.gem.internal.job

import io.kotest.core.spec.style.ShouldSpec
import kotlinx.coroutines.flow.flowOf
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import pl.edu.agh.gem.util.TestThreadExecutor
import pl.edu.agh.gem.util.createReportJob

class ReportJobConsumerTest : ShouldSpec({
    val reportJobFinder = mock<ReportJobFinder>()
    val reportJobProcessor = mock<ReportJobProcessor>()
    val reportConsumer = ReportJobConsumer(
        reportJobFinder,
        reportJobProcessor,
    )

    should("successfully process report job") {
        // given
        val reportJob = createReportJob()
        whenever(reportJobFinder.findJobToProcess()).thenReturn(flowOf(reportJob))

        // when
        reportConsumer.consume(TestThreadExecutor())

        // then
        verify(reportJobProcessor).processReportJob(reportJob)
    }
},)
