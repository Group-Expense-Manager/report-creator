package pl.edu.agh.gem.external.controller

import org.springframework.http.HttpStatus.OK
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import pl.edu.agh.gem.exception.UserWithoutGroupAccessException
import pl.edu.agh.gem.external.dto.report.GenerateReportRequest
import pl.edu.agh.gem.external.dto.report.toDomain
import pl.edu.agh.gem.internal.client.GroupManagerClient
import pl.edu.agh.gem.internal.service.ReportService
import pl.edu.agh.gem.media.InternalApiMediaType.APPLICATION_JSON_INTERNAL_VER_1
import pl.edu.agh.gem.paths.Paths.EXTERNAL
import pl.edu.agh.gem.security.GemUserId

@RestController
@RequestMapping(EXTERNAL)
class ReportController(
    private val groupManagerClient: GroupManagerClient,
    private val reportService: ReportService,
) {

    @PostMapping("generate/groups/{groupId}", consumes = [APPLICATION_JSON_INTERNAL_VER_1])
    @ResponseStatus(OK)
    fun generateReport(
        @GemUserId userId: String,
        @RequestBody generateReportRequest: GenerateReportRequest,
        @PathVariable groupId: String,
    ) {
        userId.checkIfUserHaveAccess(groupId)
        reportService.generateNewReport(generateReportRequest.toDomain(groupId, userId))
    }

    private fun String.checkIfUserHaveAccess(groupId: String) {
        groupManagerClient.getGroups(this).find { it.groupId == groupId } ?: throw UserWithoutGroupAccessException(this)
    }
}
