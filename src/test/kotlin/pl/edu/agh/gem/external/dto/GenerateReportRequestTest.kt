package pl.edu.agh.gem.external.dto

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import pl.edu.agh.gem.external.dto.report.toDomain
import pl.edu.agh.gem.util.createGenerateReportRequest

class GenerateReportRequestTest : ShouldSpec({

    should("map correctly to ReportCreation") {
        // given
        val generateReportRequest = createGenerateReportRequest()
        val groupId = "groupId123"
        val creatorId = "creatorId123"

        // when
        val result = generateReportRequest.toDomain(groupId, creatorId)

        // then
        result.format shouldBe generateReportRequest.format
        result.title shouldBe generateReportRequest.title
        result.creatorId shouldBe creatorId
        result.groupId shouldBe groupId
    }
},)
