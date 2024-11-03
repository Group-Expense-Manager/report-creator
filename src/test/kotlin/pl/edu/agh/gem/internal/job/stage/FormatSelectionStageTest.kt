package pl.edu.agh.gem.internal.job.stage

import io.kotest.core.spec.style.ShouldSpec
import org.mockito.Mockito.spy
import org.mockito.kotlin.verify
import pl.edu.agh.gem.internal.job.ReportJobState.GENERATING_XLSX_REPORT
import pl.edu.agh.gem.internal.model.report.ReportFormat.XLSX
import pl.edu.agh.gem.util.createReportJob

class FormatSelectionStageTest : ShouldSpec({

    val formatSelectionStage = spy(FormatSelectionStage())

    should("proceed to GENERATING_XLSX_REPORT state when format is XLSX") {
        // given
        val reportJob = createReportJob(format = XLSX)

        // when
        formatSelectionStage.process(reportJob)

        // then
        verify(formatSelectionStage).nextStage(reportJob, GENERATING_XLSX_REPORT)
    }
},)
