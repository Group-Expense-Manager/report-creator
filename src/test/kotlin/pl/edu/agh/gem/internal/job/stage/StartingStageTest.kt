package pl.edu.agh.gem.internal.job.stage

import io.kotest.core.spec.style.ShouldSpec
import org.mockito.Mockito.spy
import org.mockito.kotlin.verify
import pl.edu.agh.gem.internal.job.ReportJobState.FETCHING_SETTLEMENTS
import pl.edu.agh.gem.util.createReportJob

class StartingStageTest : ShouldSpec({

    val startingStage = spy(StartingStage())

    should("proceed to FETCHING_SETTLEMENTS state") {
        // given
        val reportJob = createReportJob()

        // when
        startingStage.process(reportJob)

        // then
        verify(startingStage).nextStage(reportJob, FETCHING_SETTLEMENTS)
    }
})
