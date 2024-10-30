package pl.edu.agh.gem.internal.job.stage

import org.apache.poi.ss.usermodel.CellType.NUMERIC
import org.apache.poi.ss.usermodel.CellType.STRING
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.bson.types.Binary
import org.springframework.stereotype.Component
import pl.edu.agh.gem.internal.model.finance.GroupActivities
import pl.edu.agh.gem.internal.model.finance.GroupBalances
import pl.edu.agh.gem.internal.model.finance.GroupSettlements
import pl.edu.agh.gem.internal.model.group.GroupDetails
import pl.edu.agh.gem.internal.model.user.UsersDetails
import java.io.ByteArrayOutputStream
import java.math.BigDecimal

@Component
class GenerateXLSXReportStage : GenerateReportStage() {
    override fun generateReport(
        balances: List<GroupBalances>,
        settlements: List<GroupSettlements>,
        activities: List<GroupActivities>,
        usersDetails: UsersDetails,
        groupDetails: GroupDetails,
    ): Binary {
        val workbook = XSSFWorkbook()

        workbook.writeSummary(activities, groupDetails)
        workbook.writeActivities(activities, usersDetails)
        workbook.writeBalances(balances, usersDetails)
        workbook.writeSettlements(settlements, usersDetails)

        val outputStream = ByteArrayOutputStream()
        workbook.write(outputStream)
        workbook.close()

        return outputStream.toByteArray().let(::Binary)
    }

    private fun XSSFWorkbook.writeSummary(
        activities: List<GroupActivities>,
        groupDetails: GroupDetails,
    ) {
        val summarySheet = this.createSheet("Summary")
        var rowIndex = 0

        val startDate = activities.flatMap { it.activities }.minOfOrNull { it.date }
        val endDate = activities.flatMap { it.activities }.maxOfOrNull { it.date }
        summarySheet.createRow(rowIndex++).createCell(0).setCellValue("Activities Summary for group ${groupDetails.name}")
        summarySheet.createRow(rowIndex++).createCell(0).setCellValue("Report generated from $startDate to $endDate")

        summarySheet.createRow(rowIndex++)

        summarySheet.createRow(rowIndex++).createCell(0).setCellValue("Activities Summary by Type, Status, and Currency")
        val headerRow = summarySheet.createRow(rowIndex++)
        var columnIndex = 0
        headerRow.createCell(columnIndex++).setCellValue("Type")
        headerRow.createCell(columnIndex++).setCellValue("Status")
        headerRow.createCell(columnIndex++).setCellValue("Currency")
        headerRow.createCell(columnIndex++).setCellValue("Count")
        headerRow.createCell(columnIndex++).setCellValue("Total Amount")

        activities.flatMap { groupActivity ->
            groupActivity.activities.map { activity ->
                Triple(activity.type, activity.status, groupActivity.currency) to activity.value
            }
        }.groupBy({ it.first }) { it.second }
            .forEach { (typeStatusCurrency, values) ->
                val count = values.size
                val totalAmount = values.reduce(BigDecimal::add)
                val (type, status, currency) = typeStatusCurrency

                val row = summarySheet.createRow(rowIndex++)
                columnIndex = 0
                row.createCell(columnIndex++).setCellValue(type.toString())
                row.createCell(columnIndex++).setCellValue(status.toString())
                row.createCell(columnIndex++).setCellValue(currency)
                row.createCell(columnIndex++).setCellValue(count.toDouble())
                row.createCell(columnIndex++).setCellValue(totalAmount.toDouble())
            }
    }

    private fun XSSFWorkbook.writeBalances(balances: List<GroupBalances>, usersDetails: UsersDetails) {
        val headers = listOf("User name", "Balance", "Currency")

        balances.forEach { balance ->
            val currencySheet = this.createSheet("Balances - ${balance.currency}")
            val headerRow = currencySheet.createRow(0)
            headers.forEachIndexed { index, header ->
                headerRow.createCell(index, STRING).setCellValue(header)
            }
            balance.balances.forEachIndexed { userIndex, user ->
                val row = currencySheet.createRow(userIndex + 1)
                var columnIndex = 0
                row.createCell(columnIndex++, STRING).setCellValue(usersDetails.getUserName(user.userId))
                row.createCell(columnIndex++, NUMERIC).setCellValue(user.value.toDouble())
                row.createCell(columnIndex++, STRING).setCellValue(balance.currency)
            }
        }
    }

    private fun XSSFWorkbook.writeSettlements(groupSettlements: List<GroupSettlements>, usersDetails: UsersDetails) {
        val headers = listOf("User from", "User to", "Amount", "Currency")

        groupSettlements.forEach { groupSettlement ->
            val currencySheet = this.createSheet("Settlements - ${groupSettlement.currency}")
            val headerRow = currencySheet.createRow(0)
            headers.forEachIndexed { index, header ->
                headerRow.createCell(index, STRING).setCellValue(header)
            }
            groupSettlement.settlements.forEachIndexed { settlementIndex, settlement ->
                val row = currencySheet.createRow(settlementIndex + 1)
                var columnIndex = 0
                row.createCell(columnIndex++, STRING).setCellValue(usersDetails.getUserName(settlement.fromUserId))
                row.createCell(columnIndex++, STRING).setCellValue(usersDetails.getUserName(settlement.toUserId))
                row.createCell(columnIndex++, NUMERIC).setCellValue(settlement.value.toDouble())
                row.createCell(columnIndex++, STRING).setCellValue(groupSettlement.currency)
            }
        }
    }

    private fun XSSFWorkbook.writeActivities(groupActivities: List<GroupActivities>, usersDetails: UsersDetails) {
        val headers = listOf("Title", "Type", "Author", "Participants", "Amount", "Currency", "Status")

        groupActivities.forEach { groupActivity ->
            val currencySheet = this.createSheet("Activities - ${groupActivity.currency}")
            val headerRow = currencySheet.createRow(0)
            headers.forEachIndexed { index, header ->
                headerRow.createCell(index, STRING).setCellValue(header)
            }
            groupActivity.activities.forEachIndexed { activityIndex, activity ->
                val row = currencySheet.createRow(activityIndex + 1)
                var columnIndex = 0
                row.createCell(columnIndex++, STRING).setCellValue(activity.title)
                row.createCell(columnIndex++, STRING).setCellValue(activity.type.toString())
                row.createCell(columnIndex++, STRING).setCellValue(usersDetails.getUserName(activity.creatorId))
                row.createCell(columnIndex++, STRING).setCellValue(activity.participantIds.joinToString(" ") { usersDetails.getUserName(it) })
                row.createCell(columnIndex++, NUMERIC).setCellValue(activity.value.toDouble())
                row.createCell(columnIndex++, STRING).setCellValue(groupActivity.currency)
                row.createCell(columnIndex++, STRING).setCellValue(activity.status.toString())
            }
        }
    }
}
