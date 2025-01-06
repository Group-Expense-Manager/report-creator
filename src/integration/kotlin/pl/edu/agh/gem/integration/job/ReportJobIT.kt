package pl.edu.agh.gem.integration.job

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.time.delay
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import pl.edu.agh.gem.helper.group.DummyGroup.GROUP_ID
import pl.edu.agh.gem.helper.user.DummyUser.OTHER_USER_ID
import pl.edu.agh.gem.helper.user.DummyUser.USER_ID
import pl.edu.agh.gem.integration.BaseIntegrationSpec
import pl.edu.agh.gem.integration.ability.stubEmailSenderReportNotification
import pl.edu.agh.gem.integration.ability.stubGetActivities
import pl.edu.agh.gem.integration.ability.stubGetBalances
import pl.edu.agh.gem.integration.ability.stubGetSettlements
import pl.edu.agh.gem.integration.ability.stubGetUsersDetails
import pl.edu.agh.gem.integration.ability.stubGroupManagerGroupDetails
import pl.edu.agh.gem.integration.ability.stubPostReport
import pl.edu.agh.gem.integration.ability.verifyEmailSenderReportNotification
import pl.edu.agh.gem.integration.ability.verifyPostReportUrl
import pl.edu.agh.gem.internal.persistence.ReportJobRepository
import pl.edu.agh.gem.internal.persistence.ReportRepository
import pl.edu.agh.gem.util.createActivitiesResponse
import pl.edu.agh.gem.util.createAttachment
import pl.edu.agh.gem.util.createBalancesResponse
import pl.edu.agh.gem.util.createCurrenciesDto
import pl.edu.agh.gem.util.createGroupResponse
import pl.edu.agh.gem.util.createGroupUsersDetailsResponse
import pl.edu.agh.gem.util.createMembersDto
import pl.edu.agh.gem.util.createReportJob
import pl.edu.agh.gem.util.createSettlementsResponse
import java.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

class ReportJobIT(
    @MockitoSpyBean private val clock: Clock,
    private val reportJobRepository: ReportJobRepository,
    private val reportRepository: ReportRepository,
) : BaseIntegrationSpec({

        should("process report job successfully") {
            // given
            val startedTime = testClock.instant()
            whenever(clock.instant()).thenAnswer { FIXED_TIME.plusSeconds(elapsedSeconds(startedTime)) }

            val attachmentResponse = createAttachment()
            stubPostReport(attachmentResponse, GROUP_ID, USER_ID)
            stubEmailSenderReportNotification()
            val activitiesResponse = createActivitiesResponse()
            stubGetActivities(activitiesResponse, GROUP_ID)
            val balancesResponse = createBalancesResponse()
            stubGetBalances(balancesResponse, GROUP_ID)
            val settlementsResponse = createSettlementsResponse()
            stubGetSettlements(settlementsResponse, GROUP_ID)
            val members = createMembersDto(USER_ID, OTHER_USER_ID)
            val listOfCurrencies = createCurrenciesDto("PLN", "USD", "EUR")
            val groupResponse = createGroupResponse(members = members, groupCurrencies = listOfCurrencies)
            stubGroupManagerGroupDetails(groupResponse, GROUP_ID)
            val groupUsersDetails = createGroupUsersDetailsResponse()
            stubGetUsersDetails(groupUsersDetails, GROUP_ID)

            val reportJob =
                createReportJob(
                    groupId = GROUP_ID,
                    creatorId = USER_ID,
                    balances = null,
                    activities = null,
                    settlements = null,
                    groupDetails = null,
                    usersDetails = null,
                    file = null,
                    attachmentId = null,
                    nextProcessAt = FIXED_TIME,
                )

            // when
            reportJobRepository.save(reportJob)

            // then
            waitTillExchangePlan(reportJobRepository, reportJob.id)
            val report = reportRepository.getReport(reportJob.id)
            report.shouldNotBeNull()
            report.id shouldBe reportJob.id
            report.groupId shouldBe reportJob.groupId
            report.createdAt.shouldNotBeNull()
            report.creatorId shouldBe reportJob.creatorId
            report.attachmentId shouldBe attachmentResponse.id
            report.format shouldBe reportJob.format

            verifyPostReportUrl(GROUP_ID, USER_ID)
            verifyEmailSenderReportNotification()
        }

        should("retry report job successfully") {
            // given
            val startedTime = testClock.instant()
            whenever(clock.instant()).thenAnswer { FIXED_TIME.plusSeconds(elapsedSeconds(startedTime)) }
            val attachmentResponse = createAttachment()
            stubPostReport(attachmentResponse, GROUP_ID, USER_ID, INTERNAL_SERVER_ERROR)
            stubEmailSenderReportNotification(INTERNAL_SERVER_ERROR)
            val activitiesResponse = createActivitiesResponse()
            stubGetActivities(activitiesResponse, GROUP_ID, INTERNAL_SERVER_ERROR)
            val balancesResponse = createBalancesResponse()
            stubGetBalances(balancesResponse, GROUP_ID, INTERNAL_SERVER_ERROR)
            val settlementsResponse = createSettlementsResponse()
            stubGetSettlements(settlementsResponse, GROUP_ID, INTERNAL_SERVER_ERROR)
            val members = createMembersDto(USER_ID, OTHER_USER_ID)
            val listOfCurrencies = createCurrenciesDto("PLN", "USD", "EUR")
            val groupResponse = createGroupResponse(members = members, groupCurrencies = listOfCurrencies)
            stubGroupManagerGroupDetails(groupResponse, GROUP_ID, INTERNAL_SERVER_ERROR)
            val groupUsersDetails = createGroupUsersDetailsResponse()
            stubGetUsersDetails(groupUsersDetails, GROUP_ID, INTERNAL_SERVER_ERROR)

            val reportJob =
                createReportJob(
                    groupId = GROUP_ID,
                    creatorId = USER_ID,
                    balances = null,
                    activities = null,
                    settlements = null,
                    groupDetails = null,
                    usersDetails = null,
                    file = null,
                    attachmentId = null,
                    nextProcessAt = FIXED_TIME,
                )

            // when
            reportJobRepository.save(reportJob)

            // then
            waitTillJobEndRetry(reportJobRepository, reportJob.id)
            reportRepository.getReport(reportJob.id).shouldBeNull()

            // when
            stubPostReport(attachmentResponse, GROUP_ID, USER_ID)
            stubEmailSenderReportNotification()
            stubGetActivities(activitiesResponse, GROUP_ID)
            stubGetBalances(balancesResponse, GROUP_ID)
            stubGetSettlements(settlementsResponse, GROUP_ID)
            stubGroupManagerGroupDetails(groupResponse, GROUP_ID)
            stubGetUsersDetails(groupUsersDetails, GROUP_ID)

            // then
            waitTillExchangePlan(reportJobRepository, reportJob.id)
            val report = reportRepository.getReport(reportJob.id)
            report.shouldNotBeNull()
            report.id shouldBe reportJob.id
            report.groupId shouldBe reportJob.groupId
            report.createdAt.shouldNotBeNull()
            report.creatorId shouldBe reportJob.creatorId
            report.attachmentId shouldBe attachmentResponse.id
            report.format shouldBe reportJob.format
        }
    })

private suspend fun waitTillExchangePlan(
    reportJobRepository: ReportJobRepository,
    reportJobId: String,
) {
    while (true) {
        delay(1L.seconds.toJavaDuration())
        if (reportJobRepository.findById(reportJobId) == null) {
            break
        }
    }
}

private suspend fun waitTillJobEndRetry(
    reportJobRepository: ReportJobRepository,
    reportJobId: String,
) {
    while (true) {
        delay(1L.seconds.toJavaDuration())
        val reportJob = reportJobRepository.findById(reportJobId)
        if (reportJob != null && reportJob.retry != 0L) {
            break
        }
    }
}
