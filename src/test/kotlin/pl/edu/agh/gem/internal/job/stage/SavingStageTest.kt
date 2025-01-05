package pl.edu.agh.gem.internal.job.stage

import io.kotest.core.spec.style.ShouldSpec
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.kotlin.verify
import pl.edu.agh.gem.internal.job.ReportJobState.NOTIFY
import pl.edu.agh.gem.internal.job.ReportJobState.UPLOAD_REPORT
import pl.edu.agh.gem.internal.model.report.toReport
import pl.edu.agh.gem.internal.persistence.ReportRepository
import pl.edu.agh.gem.util.createReportJob

class SavingStageTest : ShouldSpec({

    val reportRepository = mock(ReportRepository::class.java)
    val savingStage = spy(SavingStage(reportRepository))

    should("proceed to UPLOAD_REPORT state when attachmentId is null") {
        // given
        val reportJob = createReportJob(attachmentId = null)

        // when
        savingStage.process(reportJob)

        // then
        verify(savingStage).nextStage(reportJob, UPLOAD_REPORT)
    }

    should("save report and proceed to NOTIFY state when attachmentId is not null") {
        // given
        val attachmentId = "someAttachmentId"
        val reportJob = createReportJob(attachmentId = attachmentId)

        // when
        savingStage.process(reportJob)

        // then
        verify(reportRepository).save(reportJob.toReport(attachmentId = attachmentId))
        verify(savingStage).nextStage(reportJob, NOTIFY)
    }
})
