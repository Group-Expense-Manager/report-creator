package pl.edu.agh.gem.internal.persistence

import pl.edu.agh.gem.internal.model.report.Report

interface ReportRepository {
    fun save(report: Report): Report

    fun getReport(reportId: String): Report?
}
