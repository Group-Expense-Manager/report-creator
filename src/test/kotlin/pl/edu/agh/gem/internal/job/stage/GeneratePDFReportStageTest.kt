package pl.edu.agh.gem.internal.job.stage

import io.kotest.core.spec.style.ShouldSpec
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import pl.edu.agh.gem.internal.client.AttachmentStoreClient
import pl.edu.agh.gem.internal.model.finance.GroupActivities
import pl.edu.agh.gem.internal.model.finance.GroupBalances
import pl.edu.agh.gem.internal.model.finance.GroupSettlements
import pl.edu.agh.gem.internal.model.group.GroupDetails
import pl.edu.agh.gem.internal.model.user.UsersDetails
import pl.edu.agh.gem.util.ResourceLoader
import pl.edu.agh.gem.util.createGroupActivities
import pl.edu.agh.gem.util.createGroupBalances
import pl.edu.agh.gem.util.createGroupDetails
import pl.edu.agh.gem.util.createGroupSettlements
import pl.edu.agh.gem.util.createUsersDetails

class GeneratePDFReportStageTest : ShouldSpec(
    {

        val attachmentStoreClient = mock(AttachmentStoreClient::class.java)
        val generatePDFReportStage = spy(GeneratePDFReportStage(attachmentStoreClient))

        should("generate a valid XLSX report") {
            // given
            val balances: List<GroupBalances> = listOf(createGroupBalances())
            val settlements: List<GroupSettlements> = listOf(createGroupSettlements())
            val activities: List<GroupActivities> = listOf(createGroupActivities())
            val usersDetails: UsersDetails = createUsersDetails()
            val groupDetails: GroupDetails = createGroupDetails()
            whenever(attachmentStoreClient.getAttachment(groupDetails.groupId, groupDetails.attachmentId))
                .thenReturn(ResourceLoader.loadResourceAsByteArray("example-image.jpeg"))
            // when
            generatePDFReportStage.generateReport(
                balances = balances,
                settlements = settlements,
                activities = activities,
                usersDetails = usersDetails,
                groupDetails = groupDetails,
            )

            // then
            verify(generatePDFReportStage).generateReport(
                balances = balances,
                settlements = settlements,
                activities = activities,
                usersDetails = usersDetails,
                groupDetails = groupDetails,
            )
        }
    },
)
