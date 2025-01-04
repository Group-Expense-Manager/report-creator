package pl.edu.agh.gem.internal.job.stage

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldNotBe
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.bson.types.Binary
import org.mockito.Mockito.spy
import org.mockito.kotlin.verify
import pl.edu.agh.gem.internal.model.finance.GroupActivities
import pl.edu.agh.gem.internal.model.finance.GroupBalances
import pl.edu.agh.gem.internal.model.finance.GroupSettlements
import pl.edu.agh.gem.internal.model.group.GroupDetails
import pl.edu.agh.gem.internal.model.user.UsersDetails
import pl.edu.agh.gem.util.createGroupActivities
import pl.edu.agh.gem.util.createGroupBalances
import pl.edu.agh.gem.util.createGroupDetails
import pl.edu.agh.gem.util.createGroupSettlements
import pl.edu.agh.gem.util.createUsersDetails

class GenerateXLSXReportStageTest : ShouldSpec({

    val generateXLSXReportStage = spy(GenerateXLSXReportStage())

    should("generate a valid XLSX report") {
        // given
        val balances: List<GroupBalances> = listOf(createGroupBalances())
        val settlements: List<GroupSettlements> = listOf(createGroupSettlements())
        val activities: List<GroupActivities> = listOf(createGroupActivities())
        val usersDetails: UsersDetails = createUsersDetails()
        val groupDetails: GroupDetails = createGroupDetails()

        // when
        val reportFile: Binary =
            generateXLSXReportStage.generateReport(
                balances = balances,
                settlements = settlements,
                activities = activities,
                usersDetails = usersDetails,
                groupDetails = groupDetails,
            )

        // then
        verify(generateXLSXReportStage).generateReport(
            balances = balances,
            settlements = settlements,
            activities = activities,
            usersDetails = usersDetails,
            groupDetails = groupDetails,
        )

        // Verify the content of the generated report
        val workbook = XSSFWorkbook(reportFile.data.inputStream())
        workbook.use {
            it.getSheet("Summary") shouldNotBe null
            it.getSheet("Balances - USD") shouldNotBe null
            it.getSheet("Settlements - USD") shouldNotBe null
            it.getSheet("Activities - USD") shouldNotBe null
        }
    }
})
