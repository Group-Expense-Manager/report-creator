package pl.edu.agh.gem.external.persistence.report

import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.stereotype.Repository
import pl.edu.agh.gem.internal.model.report.Report
import pl.edu.agh.gem.internal.persistence.ReportRepository
import pl.edu.agh.gem.metrics.MeteredRepository

@Repository
@MeteredRepository
class MongoReportRepository(
    private val mongoOperations: MongoOperations,
) : ReportRepository {
    override fun save(report: Report): Report {
        return mongoOperations.save(report.toEntity()).toDomain()
    }

    override fun getReport(reportId: String): Report? {
        return mongoOperations.findById(reportId, ReportEntity::class.java)?.toDomain()
    }
}
