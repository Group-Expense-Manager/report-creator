package pl.edu.agh.gem.internal.job

import io.kotest.core.spec.style.ShouldSpec
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import pl.edu.agh.gem.internal.job.ReportJobState.STARTING
import pl.edu.agh.gem.internal.persistence.ReportJobRepository
import pl.edu.agh.gem.util.createReportJob

class ReportJobProcessorTest : ShouldSpec({
    val reportJobSelector = mock<ReportJobSelector>()
    val reportJobRepository = mock<ReportJobRepository>()
    val reportJobProcessor = ReportJobProcessor(
        reportJobSelector,
        reportJobRepository,
    )

    should("handle NextStage state transition") {
        // given
        val reportJob = createReportJob()
        val nextStage = NextStage(
            reportJob.copy(state = STARTING),
            newState = STARTING,
        )
        val stateProcessor = mock<ProcessingStage> {
            on { process(reportJob) } doReturn nextStage
        }
        whenever(reportJobSelector.select(any())).thenReturn(stateProcessor)

        // when
        reportJobProcessor.processReportJob(reportJob)

        // then
        verify(reportJobRepository).save(nextStage.reportJob.copy(state = nextStage.newState))
    }

    should("handle StageSuccess state transition") {
        // given
        val reportJob = createReportJob()
        val stateProcessor = mock<ProcessingStage> {
            on { process(reportJob) } doReturn StageSuccess
        }
        whenever(reportJobSelector.select(any())).thenReturn(stateProcessor)

        // when
        reportJobProcessor.processReportJob(reportJob)

        // then
        verify(reportJobRepository).remove(reportJob)
    }

    should("handle StageFailure state transition") {
        // given
        val reportJob = createReportJob()
        val stateProcessor = mock<ProcessingStage> {
            on { process(reportJob) } doReturn StageFailure(Exception())
        }
        whenever(reportJobSelector.select(any())).thenReturn(stateProcessor)

        // when
        reportJobProcessor.processReportJob(reportJob)

        // then
        verify(reportJobRepository).remove(reportJob)
    }

    should("handle StageRetry state transition") {
        // given
        val reportJob = createReportJob()
        val stateProcessor = mock<ProcessingStage> {
            on { process(reportJob) } doReturn StageRetry
        }
        whenever(reportJobSelector.select(any())).thenReturn(stateProcessor)

        // when
        reportJobProcessor.processReportJob(reportJob)

        // then
        verify(reportJobRepository).updateNextProcessAtAndRetry(reportJob)
    }
},)
