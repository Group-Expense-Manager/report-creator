package pl.edu.agh.gem.internal.job.stage

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import pl.edu.agh.gem.internal.job.StageFailure
import pl.edu.agh.gem.util.createReportJob

class ErrorStageTest : ShouldSpec({

    val errorStage = ErrorStage()

    should("return StageFailure when process is called") {
        // given
        val reportJob = createReportJob()

        // when
        val result = errorStage.process(reportJob)

        // then
        result::class shouldBe StageFailure::class
    }
},)
