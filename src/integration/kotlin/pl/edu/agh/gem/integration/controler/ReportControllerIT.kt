package pl.edu.agh.gem.integration.controler

import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.OK
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import pl.edu.agh.gem.assertion.shouldHaveHttpStatus
import pl.edu.agh.gem.external.dto.group.GroupDTO
import pl.edu.agh.gem.helper.group.DummyGroup.GROUP_ID
import pl.edu.agh.gem.helper.group.DummyGroup.OTHER_GROUP_ID
import pl.edu.agh.gem.helper.user.DummyUser.USER_ID
import pl.edu.agh.gem.helper.user.createGemUser
import pl.edu.agh.gem.integration.BaseIntegrationSpec
import pl.edu.agh.gem.integration.ability.ServiceTestClient
import pl.edu.agh.gem.integration.ability.stubGroupManagerUserGroups
import pl.edu.agh.gem.internal.persistence.ReportJobRepository
import pl.edu.agh.gem.util.createGenerateReportRequest
import pl.edu.agh.gem.util.createUserGroupsResponse

class ReportControllerIT(
    private val service: ServiceTestClient,
    @MockitoSpyBean private val reportJobRepository: ReportJobRepository,
) : BaseIntegrationSpec({

        should("generate report successfully") {
            // given
            val userGroups = listOf(GroupDTO(GROUP_ID), GroupDTO(OTHER_GROUP_ID))
            val userGroupsResponse = createUserGroupsResponse(userGroups)
            stubGroupManagerUserGroups(userGroupsResponse, USER_ID)
            val user = createGemUser(USER_ID)
            val groupId = GROUP_ID
            val generateReportRequest = createGenerateReportRequest()

            // when
            val response = service.generateReport(user, groupId, generateReportRequest)

            // then
            response shouldHaveHttpStatus OK
            verify(reportJobRepository).save(any())
        }

        should("return FORBIDDEN when user does not have access to the group") {
            // given
            val userGroups = listOf(GroupDTO(GROUP_ID), GroupDTO(OTHER_GROUP_ID))
            val userGroupsResponse = createUserGroupsResponse(userGroups)
            stubGroupManagerUserGroups(userGroupsResponse, USER_ID)
            val user = createGemUser(USER_ID)
            val groupId = "testGroup"
            val generateReportRequest = createGenerateReportRequest()

            // when
            val response = service.generateReport(user, groupId, generateReportRequest)

            // then
            response shouldHaveHttpStatus FORBIDDEN
        }
    })
