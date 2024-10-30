package pl.edu.agh.gem.internal.service

import io.kotest.core.spec.style.ShouldSpec
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import pl.edu.agh.gem.internal.model.report.ReportJob
import pl.edu.agh.gem.internal.persistence.ReportJobRepository
import pl.edu.agh.gem.util.createReportCreation
import java.time.Clock
import java.time.Instant

class ReportServiceTest : ShouldSpec({

    val reportJobRepository = mock<ReportJobRepository>()
    val clock = mock<Clock>()
    val reportService = ReportService(reportJobRepository, clock)

    should("save report job when generating new report") {
        // given
        val reportCreation = createReportCreation()
        val reportJob = mock<ReportJob>()
        whenever(clock.instant()).thenReturn(Instant.now())
        whenever(reportJobRepository.save(any())).thenReturn(reportJob)

        // when
        reportService.generateNewReport(reportCreation)

        // then
        verify(reportJobRepository).save(any())
    }
},)
