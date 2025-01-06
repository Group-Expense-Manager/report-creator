package pl.edu.agh.gem.internal.job.stage

import org.apache.poi.ss.usermodel.CellType.NUMERIC
import org.apache.poi.ss.usermodel.CellType.STRING
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.Font
import org.apache.poi.ss.usermodel.IndexedColors.BLACK
import org.apache.poi.ss.usermodel.IndexedColors.GREY_25_PERCENT
import org.apache.poi.ss.usermodel.IndexedColors.LIGHT_BLUE
import org.apache.poi.ss.usermodel.IndexedColors.WHITE
import org.apache.poi.xssf.usermodel.XSSFCellStyle
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
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
        val headerStyle = createHeaderStyle(workbook)
        val titleStyle = createTitleStyle(workbook)
        val subHeaderStyle = createSubHeaderStyle(workbook)
        val summaryHeaderStyle = createSummaryHeaderStyle(workbook)
        val summaryCellStyle = createSummaryCellStyle(workbook)
        val dataCellStyle = createDataCellStyle(workbook)
        workbook.writeSummary(
            activities,
            groupDetails,
            titleStyle,
            subHeaderStyle,
            summaryHeaderStyle,
            summaryCellStyle,
        )
        workbook.writeActivities(activities, usersDetails, headerStyle, dataCellStyle)
        workbook.writeBalances(balances, usersDetails, headerStyle, dataCellStyle)
        workbook.writeSettlements(settlements, usersDetails, headerStyle, dataCellStyle)
        val outputStream = ByteArrayOutputStream()
        workbook.write(outputStream)
        workbook.close()
        return outputStream.toByteArray().let(::Binary)
    }

    private fun XSSFWorkbook.writeSummary(
        activities: List<GroupActivities>,
        groupDetails: GroupDetails,
        titleStyle: XSSFCellStyle,
        subHeaderStyle: XSSFCellStyle,
        summaryHeaderStyle: XSSFCellStyle,
        summaryCellStyle: XSSFCellStyle,
    ) {
        val summarySheet = this.createSheet("Summary")
        summarySheet.setColumnWidth(0, 30 * 256)

        var rowIndex = 0

        val startDate = activities.flatMap { it.activities }.minOfOrNull { it.date }
        val endDate = activities.flatMap { it.activities }.maxOfOrNull { it.date }

        val formattedStartDate = startDate?.let { dateFormatter.format(it) }
        val formattedEndDate = endDate?.let { dateFormatter.format(it) }

        summarySheet.createRow(rowIndex++).apply {
            createCell(0).setCellValue("Activities Summary for group ${groupDetails.name}")
            getCell(0).cellStyle = titleStyle
        }
        summarySheet.createRow(rowIndex++).apply {
            createCell(0).setCellValue("Report generated from $formattedStartDate to $formattedEndDate")
            getCell(0).cellStyle = subHeaderStyle
        }
        summarySheet.createRow(rowIndex++)
        summarySheet.createRow(rowIndex++).apply {
            createCell(0).setCellValue("Activities Summary by Type, Status, and Currency")
            getCell(0).cellStyle = summaryHeaderStyle
        }
        val headerRow = summarySheet.createRow(rowIndex++)
        var columnIndex = 0
        headerRow.createCell(columnIndex++).apply {
            setCellValue("Type")
            cellStyle = summaryHeaderStyle
        }
        headerRow.createCell(columnIndex++).apply {
            setCellValue("Status")
            cellStyle = summaryHeaderStyle
        }
        headerRow.createCell(columnIndex++).apply {
            setCellValue("Currency")
            cellStyle = summaryHeaderStyle
        }
        headerRow.createCell(columnIndex++).apply {
            setCellValue("Count")
            cellStyle = summaryHeaderStyle
        }
        headerRow.createCell(columnIndex++).apply {
            setCellValue("Total Amount")
            cellStyle = summaryHeaderStyle
        }
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
                row.createCell(columnIndex++).apply {
                    setCellValue(type.toString())
                    cellStyle = summaryCellStyle
                }
                row.createCell(columnIndex++).apply {
                    setCellValue(status.toString())
                    cellStyle = summaryCellStyle
                }
                row.createCell(columnIndex++).apply {
                    setCellValue(currency)
                    cellStyle = summaryCellStyle
                }
                row.createCell(columnIndex++).apply {
                    setCellValue(count.toDouble())
                    cellStyle = summaryCellStyle
                }
                row.createCell(columnIndex++).apply {
                    setCellValue(totalAmount.toDouble())
                    cellStyle = summaryCellStyle
                }
            }

        for (i in 0..4) {
            summarySheet.autoSizeColumn(i)
        }
    }

    private fun XSSFWorkbook.writeBalances(
        balances: List<GroupBalances>,
        usersDetails: UsersDetails,
        headerStyle: XSSFCellStyle,
        dataCellStyle: XSSFCellStyle,
    ) {
        val headers = listOf("User name", "Balance", "Currency")
        balances.forEach { balance ->
            val currencySheet = this.createSheet("Balances - ${balance.currency}")
            val headerRow = currencySheet.createRow(0)
            headers.forEachIndexed { index, header ->
                headerRow.createCell(index, STRING).apply {
                    setCellValue(header)
                    cellStyle = headerStyle
                }
            }
            balance.balances.forEachIndexed { userIndex, user ->
                val row = currencySheet.createRow(userIndex + 1)
                var columnIndex = 0
                row.createCell(columnIndex++, STRING).apply {
                    setCellValue(usersDetails.getUserName(user.userId))
                    cellStyle = dataCellStyle
                }
                row.createCell(columnIndex++, NUMERIC).apply {
                    setCellValue(user.value.toDouble())
                    cellStyle = dataCellStyle
                }
                row.createCell(columnIndex, STRING).apply {
                    setCellValue(balance.currency)
                    cellStyle = dataCellStyle
                }
            }

            for (i in headers.indices) {
                currencySheet.autoSizeColumn(i)
            }
        }
    }

    private fun XSSFWorkbook.writeSettlements(
        groupSettlements: List<GroupSettlements>,
        usersDetails: UsersDetails,
        headerStyle: XSSFCellStyle,
        dataCellStyle: XSSFCellStyle,
    ) {
        val headers = listOf("User from", "User to", "Amount", "Currency")
        groupSettlements.forEach { groupSettlement ->
            val currencySheet = this.createSheet("Settlements - ${groupSettlement.currency}")
            val headerRow = currencySheet.createRow(0)
            headers.forEachIndexed { index, header ->
                headerRow.createCell(index, STRING).apply {
                    setCellValue(header)
                    cellStyle = headerStyle
                }
            }
            groupSettlement.settlements.forEachIndexed { settlementIndex, settlement ->
                val row = currencySheet.createRow(settlementIndex + 1)
                var columnIndex = 0
                row.createCell(columnIndex++, STRING).apply {
                    setCellValue(usersDetails.getUserName(settlement.fromUserId))
                    cellStyle = dataCellStyle
                }
                row.createCell(columnIndex++, STRING).apply {
                    setCellValue(usersDetails.getUserName(settlement.toUserId))
                    cellStyle = dataCellStyle
                }
                row.createCell(columnIndex++, NUMERIC).apply {
                    setCellValue(settlement.value.toDouble())
                    cellStyle = dataCellStyle
                }
                row.createCell(columnIndex, STRING).apply {
                    setCellValue(groupSettlement.currency)
                    cellStyle = dataCellStyle
                }
            }

            for (i in headers.indices) {
                currencySheet.autoSizeColumn(i)
            }
        }
    }

    private fun XSSFWorkbook.writeActivities(
        groupActivities: List<GroupActivities>,
        usersDetails: UsersDetails,
        headerStyle: XSSFCellStyle,
        dataCellStyle: XSSFCellStyle,
    ) {
        val headers = listOf("Title", "Type", "Author", "Participants", "Amount", "Currency", "Status")
        groupActivities.forEach { groupActivity ->
            val currencySheet = this.createSheet("Activities - ${groupActivity.currency}")
            val headerRow = currencySheet.createRow(0)
            headers.forEachIndexed { index, header ->
                headerRow.createCell(index, STRING).apply {
                    setCellValue(header)
                    cellStyle = headerStyle
                }
            }
            groupActivity.activities.forEachIndexed { activityIndex, activity ->
                val row = currencySheet.createRow(activityIndex + 1)
                var columnIndex = 0
                row.createCell(columnIndex++, STRING).apply {
                    setCellValue(activity.title)
                    cellStyle = dataCellStyle
                }
                row.createCell(columnIndex++, STRING).apply {
                    setCellValue(activity.type.toString())
                    cellStyle = dataCellStyle
                }
                row.createCell(columnIndex++, STRING).apply {
                    setCellValue(usersDetails.getUserName(activity.creatorId))
                    cellStyle = dataCellStyle
                }
                row.createCell(columnIndex++, STRING).apply {
                    setCellValue(
                        activity.participantIds.joinToString(", ") { usersDetails.getUserName(it) },
                    )
                    cellStyle = dataCellStyle
                }
                row.createCell(columnIndex++, NUMERIC).apply {
                    setCellValue(activity.value.toDouble())
                    cellStyle = dataCellStyle
                }
                row.createCell(columnIndex++, STRING).apply {
                    setCellValue(groupActivity.currency)
                    cellStyle = dataCellStyle
                }
                row.createCell(columnIndex, STRING).apply {
                    setCellValue(activity.status.toString())
                    cellStyle = dataCellStyle
                }
            }

            for (i in headers.indices) {
                currencySheet.autoSizeColumn(i)
            }
        }
    }

    private fun createHeaderStyle(workbook: XSSFWorkbook): XSSFCellStyle {
        val headerFont: Font = workbook.createFont()
        headerFont.bold = true
        headerFont.color = whiteColor.index
        val headerStyle: XSSFCellStyle = workbook.createCellStyle() as XSSFCellStyle
        headerStyle.setFillForegroundColor(primaryColor.index)
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND)
        headerStyle.setFont(headerFont)
        return headerStyle
    }

    private fun createTitleStyle(workbook: XSSFWorkbook): XSSFCellStyle {
        val titleFont = workbook.createFont()
        titleFont.bold = true
        titleFont.fontHeightInPoints = 16
        titleFont.color = blackColor.index
        val titleStyle = workbook.createCellStyle() as XSSFCellStyle
        titleStyle.setFont(titleFont)
        return titleStyle
    }

    private fun createSubHeaderStyle(workbook: XSSFWorkbook): XSSFCellStyle {
        val subHeaderFont = workbook.createFont()
        subHeaderFont.italic = true
        subHeaderFont.fontHeightInPoints = 14
        subHeaderFont.color = blackColor.index
        val subHeaderStyle = workbook.createCellStyle() as XSSFCellStyle
        subHeaderStyle.setFont(subHeaderFont)
        return subHeaderStyle
    }

    private fun createSummaryHeaderStyle(workbook: XSSFWorkbook): XSSFCellStyle {
        val summaryHeaderFont = workbook.createFont()
        summaryHeaderFont.bold = true
        summaryHeaderFont.color = blackColor.index
        val summaryHeaderStyle = workbook.createCellStyle() as XSSFCellStyle
        summaryHeaderStyle.setFillForegroundColor(secondaryColor.index)
        summaryHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND)
        summaryHeaderStyle.setFont(summaryHeaderFont)
        return summaryHeaderStyle
    }

    private fun createSummaryCellStyle(workbook: XSSFWorkbook): XSSFCellStyle {
        val summaryCellFont = workbook.createFont()
        summaryCellFont.color = blackColor.index
        val summaryCellStyle = workbook.createCellStyle() as XSSFCellStyle
        summaryCellStyle.setFillForegroundColor(GREY_25_PERCENT.index)
        summaryCellStyle.setFillPattern(FillPatternType.THIN_FORWARD_DIAG)
        summaryCellStyle.setFont(summaryCellFont)
        return summaryCellStyle
    }

    private fun createDataCellStyle(workbook: XSSFWorkbook): XSSFCellStyle {
        val dataCellFont = workbook.createFont()
        dataCellFont.color = blackColor.index
        val dataCellStyle = workbook.createCellStyle() as XSSFCellStyle
        dataCellStyle.setFont(dataCellFont)
        return dataCellStyle
    }

    companion object {
        val primaryColor = LIGHT_BLUE
        val secondaryColor = GREY_25_PERCENT
        val whiteColor = WHITE
        val blackColor = BLACK
        val dateFormatter: DateTimeFormatter =
            DateTimeFormatter.ofPattern("dd MMM YYYY HH:mm")
                .withZone(ZoneId.systemDefault())
    }
}
