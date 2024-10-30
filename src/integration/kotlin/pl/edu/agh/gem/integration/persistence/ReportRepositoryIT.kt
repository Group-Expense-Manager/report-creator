package pl.edu.agh.gem.integration.persistence

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import pl.edu.agh.gem.integration.BaseIntegrationSpec
import pl.edu.agh.gem.internal.persistence.ReportRepository
import pl.edu.agh.gem.util.createReport

class ReportRepositoryIT(
    private val reportRepository: ReportRepository,
) : BaseIntegrationSpec({

    should("save and retrieve report by id") {
        // given
        val report = createReport()

        // when
        val savedReport = reportRepository.save(report)

        // then
        savedReport.id shouldBe report.id

        // when
        val foundReport = reportRepository.getReport(report.id)

        // then
        foundReport.shouldNotBeNull()
        foundReport.id shouldBe report.id
    }

    should("return null when retrieving non-existing report by id") {
        // given
        val nonExistingReportId = "non-existing-id"

        // when
        val foundReport = reportRepository.getReport(nonExistingReportId)

        // then
        foundReport.shouldBeNull()
    }
},)
