package pl.edu.agh.gem.external.persistence.job

import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.stereotype.Repository
import pl.edu.agh.gem.config.ReportJobProcessorProperties
import pl.edu.agh.gem.internal.model.report.ReportJob
import pl.edu.agh.gem.internal.persistence.MissingReconciliationJobException
import pl.edu.agh.gem.internal.persistence.ReportJobRepository
import java.time.Clock
import java.time.Duration

@Repository
class MongoReportJobRepository(
    private val mongoOperations: MongoOperations,
    private val reportJobProcessorProperties: ReportJobProcessorProperties,
    private val clock: Clock,
) : ReportJobRepository {
    override fun save(reportJob: ReportJob): ReportJob {
        return mongoOperations.save(reportJob.toEntity()).toDomain()
    }

    override fun findJobToProcessAndLock(): ReportJob? {
        val query = Query.query(Criteria.where(ReportJobEntity::nextProcessAt.name).lte(clock.instant()))
        val update = Update()
            .set(ReportJobEntity::nextProcessAt.name, clock.instant().plus(reportJobProcessorProperties.lockTime))
        val options = FindAndModifyOptions.options().returnNew(false).upsert(false)
        return mongoOperations.findAndModify(query, update, options, ReportJobEntity::class.java)?.toDomain()
    }

    override fun updateNextProcessAtAndRetry(reportJob: ReportJob): ReportJob {
        val query = Query.query(Criteria.where(ReportJobEntity::id.name).isEqualTo(reportJob.id))
        val update = Update()
            .set(ReportJobEntity::nextProcessAt.name, clock.instant().plus(getDelay(reportJob.retry)))
            .set(ReportJobEntity::retry.name, reportJob.retry + 1)
        val options = FindAndModifyOptions.options().returnNew(true).upsert(false)
        mongoOperations.findAll(ReportJobEntity::class.java)
        return mongoOperations.findAndModify(query, update, options, ReportJobEntity::class.java)?.toDomain()
            ?: throw MissingReconciliationJobException(reportJob)
    }

    override fun remove(reportJob: ReportJob) {
        val query = Query.query(Criteria.where(ReportJobEntity::id.name).isEqualTo(reportJob.id))
        mongoOperations.remove(query, ReportJobEntity::class.java)
    }

    override fun findById(id: String): ReportJob? {
        return mongoOperations.findById(id, ReportJobEntity::class.java)?.toDomain()
    }
    
    private fun getDelay(retry: Long): Duration {
        return reportJobProcessorProperties
            .retryDelays
            .getOrNull(retry.toInt())
            ?: reportJobProcessorProperties.retryDelays.last()
    }
}
