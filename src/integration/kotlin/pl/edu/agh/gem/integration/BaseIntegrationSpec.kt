package pl.edu.agh.gem.integration

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.core.spec.style.ShouldSpec
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import pl.edu.agh.gem.AppRunner
import pl.edu.agh.gem.integration.environment.ProjectConfig
import java.time.Clock
import java.time.Instant

@SpringBootTest(
    classes = [AppRunner::class],
    webEnvironment = RANDOM_PORT,
)
@ActiveProfiles("integration")
abstract class BaseIntegrationSpec(body: ShouldSpec.() -> Unit) : ShouldSpec(body) {
    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun injectContainerData(registry: DynamicPropertyRegistry) {
            logger.info { "Injecting configuration" }
            ProjectConfig.updateConfiguration(registry)
        }

        val objectMappper = jacksonObjectMapper().registerModules(JavaTimeModule())

        val testClock = Clock.systemUTC()
        val FIXED_TIME = Instant.parse("2021-01-01T00:00:00Z")
        private val logger = KotlinLogging.logger {}

        fun elapsedSeconds(start: Instant): Long {
            return testClock.instant().epochSecond - start.epochSecond
        }
    }
}
