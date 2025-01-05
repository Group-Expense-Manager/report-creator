package pl.edu.agh.gem.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.edu.agh.gem.internal.job.ReportJobConsumer
import pl.edu.agh.gem.internal.job.ReportJobFinder
import pl.edu.agh.gem.internal.job.ReportJobProcessor
import pl.edu.agh.gem.internal.persistence.ReportJobRepository
import pl.edu.agh.gem.threads.ExecutorConfig
import pl.edu.agh.gem.threads.ExecutorFactory
import java.time.Duration
import java.util.concurrent.Executor

@Configuration
class ReportJobConfig {
    @Bean(destroyMethod = "destroy")
    @ConditionalOnProperty(prefix = REPORT_PROCESSOR_PREFIX, name = ["enabled"], havingValue = "true")
    fun financialReconciliationJobConsumer(
        jobConsumerExecutor: Executor,
        reportJobFinder: ReportJobFinder,
        reportJobProcessor: ReportJobProcessor,
    ): ReportJobConsumer {
        val reportJobConsumer =
            ReportJobConsumer(
                reportJobFinder,
                reportJobProcessor,
            )
        reportJobConsumer.consume(jobConsumerExecutor)
        return reportJobConsumer
    }

    @Bean
    fun financialReconciliationJobFinder(
        jobProducerExecutor: Executor,
        reportJobProcessorProperties: ReportJobProcessorProperties,
        reportJobRepository: ReportJobRepository,
    ) = ReportJobFinder(
        jobProducerExecutor,
        reportJobRepository,
        reportJobProcessorProperties,
    )

    @Bean
    fun jobConsumerExecutor(
        executorFactory: ExecutorFactory,
        settings: ReportJobExecutorProperties,
    ): Executor {
        val config =
            ExecutorConfig(
                corePoolSize = settings.corePoolSize,
                maxPoolSize = settings.maxPoolSize,
                taskQueueSize = settings.queueCapacity,
                threadPoolName = CONSUMER_POOL,
            )
        return executorFactory.createExecutor(config)
    }

    @Bean
    fun jobProducerExecutor(
        executorFactory: ExecutorFactory,
        settings: ReportJobProducerProperties,
    ): Executor {
        val config =
            ExecutorConfig(
                corePoolSize = settings.corePoolSize,
                maxPoolSize = settings.maxPoolSize,
                taskQueueSize = settings.queueCapacity,
                threadPoolName = PRODUCER_POOL,
            )
        return executorFactory.createExecutor(config)
    }

    companion object {
        private const val CONSUMER_POOL = "report-job-consumer-pool"
        private const val PRODUCER_POOL = "report-job-producer-pool"
        private const val REPORT_PROCESSOR_PREFIX = "report-job-processor"
    }
}

@ConfigurationProperties("report-job-executor")
data class ReportJobExecutorProperties(
    val corePoolSize: Int,
    val maxPoolSize: Int,
    val queueCapacity: Int,
)

@ConfigurationProperties("report-job-producer")
data class ReportJobProducerProperties(
    val corePoolSize: Int,
    val maxPoolSize: Int,
    val queueCapacity: Int,
)

@ConfigurationProperties("report-job-processor")
data class ReportJobProcessorProperties(
    val lockTime: Duration,
    val emptyCandidateDelay: Duration,
    val retryDelays: List<Duration>,
)
