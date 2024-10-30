package pl.edu.agh.gem.internal.job.stage

import org.springframework.stereotype.Component
import pl.edu.agh.gem.internal.job.ProcessingStage
import pl.edu.agh.gem.internal.job.ReportJobState.NOTIFY
import pl.edu.agh.gem.internal.job.ReportJobState.UPLOAD_REPORT
import pl.edu.agh.gem.internal.job.StageResult
import pl.edu.agh.gem.internal.model.report.ReportJob
import pl.edu.agh.gem.internal.model.report.toReport
import pl.edu.agh.gem.internal.persistence.ReportRepository

@Component
class SavingStage(
    private val reportRepository: ReportRepository,
) : ProcessingStage() {
    override fun process(reportJob: ReportJob): StageResult {
        logger.info { "Saving report job: $reportJob" }
        if (reportJob.attachmentId == null) return nextStage(reportJob, UPLOAD_REPORT)
        reportRepository.save(reportJob.toReport(attachmentId = reportJob.attachmentId))
        return nextStage(reportJob, NOTIFY)
    }
}
