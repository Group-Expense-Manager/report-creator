package pl.edu.agh.gem.integration.persistence

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.mock.mockito.SpyBean
import pl.edu.agh.gem.config.ReportJobProcessorProperties
import pl.edu.agh.gem.integration.BaseIntegrationSpec
import pl.edu.agh.gem.internal.persistence.MissingReportJobException
import pl.edu.agh.gem.internal.persistence.ReportJobRepository
import pl.edu.agh.gem.util.createReportJob
import java.time.Clock

class ReportJobRepositoryIT(
    @SpyBean private val clock: Clock,
    private val reportJobRepository: ReportJobRepository,
    private val reportJobProcessorProperties: ReportJobProcessorProperties,
) : BaseIntegrationSpec({

    should("save and report rate job by id") {
        // given
        val reportJob = createReportJob()

        // when
        val savedJob = reportJobRepository.save(reportJob)

        // then
        savedJob.id shouldBe reportJob.id

        // when
        val foundJob = reportJobRepository.findById(reportJob.id)

        // then
        foundJob.shouldNotBeNull()
        foundJob.id shouldBe reportJob.id
    }

    should("find and lock job to process") {
        // given
        val exchangeRateJob = createReportJob(
            nextProcessAt = FIXED_TIME,
        )
        reportJobRepository.save(exchangeRateJob)

        // when
        reportJobRepository.findJobToProcessAndLock()
        val jobToProcess = reportJobRepository.findById(exchangeRateJob.id)

        // then
        jobToProcess.shouldNotBeNull()
        jobToProcess.id shouldBe exchangeRateJob.id
        jobToProcess.nextProcessAt shouldBe FIXED_TIME.plus(reportJobProcessorProperties.lockTime)
    }

    should("update nextProcessAt and retry count") {
        // given
        val exchangeRateJob = createReportJob(
            nextProcessAt = FIXED_TIME,
            retry = 0L,
        )
        reportJobRepository.save(exchangeRateJob)

        // when
        val updatedJob = reportJobRepository.updateNextProcessAtAndRetry(exchangeRateJob)

        // then
        updatedJob.shouldNotBeNull()
        updatedJob.nextProcessAt shouldBe FIXED_TIME.plus(reportJobProcessorProperties.retryDelays.first())
        updatedJob.retry shouldBe 1
    }

    should("remove report job") {
        // given
        val exchangeRateJob = createReportJob()
        reportJobRepository.save(exchangeRateJob)

        // when
        reportJobRepository.remove(exchangeRateJob)

        // then
        val foundJob = reportJobRepository.findById(exchangeRateJob.id)
        foundJob.shouldBeNull()
    }

    should("throw MissingReportJobException when updating non-existing job") {
        // given
        val nonExistingJob = createReportJob(id = "non-existing-id", nextProcessAt = FIXED_TIME, retry = 0L)

        // when & then
        shouldThrow<MissingReportJobException> {
            reportJobRepository.updateNextProcessAtAndRetry(nonExistingJob)
        }
    }

    should("return null when finding non-existing job by id") {
        // given
        val nonExistingJobId = "non-existing-id"

        // when
        val foundJob = reportJobRepository.findById(nonExistingJobId)

        // then
        foundJob.shouldBeNull()
    }

    should("return null when no job to process is found") {
        // given
        val exchangeRateJob = createReportJob(
            nextProcessAt = FIXED_TIME.plusSeconds(3600),
        )
        reportJobRepository.save(exchangeRateJob)

        // when
        val jobToProcess = reportJobRepository.findJobToProcessAndLock()

        // then
        jobToProcess.shouldBeNull()
    }
},)
