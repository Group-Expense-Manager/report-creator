package pl.edu.agh.gem.internal.persistence

import pl.edu.agh.gem.internal.model.report.ReportJob

interface ReportJobRepository {
    fun save(reportJob: ReportJob): ReportJob

    fun findJobToProcessAndLock(): ReportJob?

    fun updateNextProcessAtAndRetry(reportJob: ReportJob): ReportJob?

    fun remove(reportJob: ReportJob)

    fun findById(id: String): ReportJob?
}

class MissingReportJobException(reportJob: ReportJob) : RuntimeException(
    "No report job found, $reportJob",
)
