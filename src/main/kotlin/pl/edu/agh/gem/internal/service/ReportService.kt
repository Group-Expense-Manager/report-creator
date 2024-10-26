package pl.edu.agh.gem.internal.service

import org.springframework.stereotype.Service
import pl.edu.agh.gem.internal.model.report.ReportCreation
import pl.edu.agh.gem.internal.model.report.toReportJob
import pl.edu.agh.gem.internal.persistence.ReportJobRepository
import java.time.Clock

@Service
class ReportService(
        private val reportJobRepository: ReportJobRepository,
        private val clock:Clock
){
    fun generateNewReport(reportCreation: ReportCreation) {
        reportJobRepository.save(reportCreation.toReportJob(clock))
    }
}
