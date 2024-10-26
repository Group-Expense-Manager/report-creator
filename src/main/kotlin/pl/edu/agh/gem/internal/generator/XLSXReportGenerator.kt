package pl.edu.agh.gem.internal.generator

import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.CellType.NUMERIC
import org.apache.poi.ss.usermodel.CellType.STRING
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import pl.edu.agh.gem.internal.model.finance.Activity
import pl.edu.agh.gem.internal.model.finance.Balances
import pl.edu.agh.gem.internal.model.finance.Settlements
import java.io.ByteArrayOutputStream

object XLSXReportGenerator {

    fun generateReport(
        balances:List<Balances>,
        settlements:List<Settlements>,
        activities:List<Activity>
    ): ByteArray {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Sample Data")
        
        val headerRow = sheet.createRow(0)
        val headers = listOf("ID", "Name", "Age")
        headers.forEachIndexed { index, header ->
            headerRow.createCell(index, STRING).setCellValue(header)
        }

        val data = listOf(
                listOf("1", "Alice", "23"),
                listOf("2", "Bob", "30"),
                listOf("3", "Charlie", "40")
        )

        data.forEachIndexed { rowIndex, rowData ->
            val row = sheet.createRow(rowIndex + 1)
            rowData.forEachIndexed { colIndex, value ->
                row.createCell(colIndex, STRING).setCellValue(value)
            }
        }

        val outputStream = ByteArrayOutputStream()
        workbook.write(outputStream)
        workbook.close()

        return outputStream.toByteArray()
    }
    
    fun XSSFWorkbook.writeBalances(balances: List<Balances>) {
        val sheet = createSheet("Balances")
        val headerRow = sheet.createRow(0)
        val headers = listOf("User ID", "Balance", "Currency")
        headers.forEachIndexed { index, header ->
            headerRow.createCell(index, STRING).setCellValue(header)
        }

        balances.forEachIndexed { rowIndex, balance ->
            balance.users.forEachIndexed { userIndex, user ->
                val row = sheet.createRow(rowIndex * balances.size + userIndex + 1)
                row.createCell(0, STRING).setCellValue(user.userId)
                row.createCell(1, NUMERIC).setCellValue(user.value.toDouble())
                row.createCell(2, STRING).setCellValue(balance.currency)
            }
        }
    }
    
    fun XSSFWorkbook.writeSettlements(settlements: List<Settlements>) {
        val sheet = createSheet("Settlements")
        val headerRow = sheet.createRow(0)
        val headers = listOf("Group ID", "Currency", "Status")
        headers.forEachIndexed { index, header ->
            headerRow.createCell(index, STRING).setCellValue(header)
        }

        settlements.forEachIndexed { rowIndex, settlement ->
            val row = sheet.createRow(rowIndex + 1)
            row.createCell(0, STRING).setCellValue(settlement.groupId)
            row.createCell(1, STRING).setCellValue(settlement.currency)
            row.createCell(2, STRING).setCellValue(settlement.status.name)
        }
    }
    
    fun XSSFWorkbook.writeActivities(activities: List<Activity>) {
        val sheet = createSheet("Activities")
        val headerRow = sheet.createRow(0)
        val headers = listOf("ID", "Name", "Date", "Amount", "Currency")
        headers.forEachIndexed { index, header ->
            headerRow.createCell(index, STRING).setCellValue(header)
        }

        activities.forEachIndexed { rowIndex, activity ->
            val row = sheet.createRow(rowIndex + 1)
            row.createCell(0, STRING).setCellValue(activity.id)
            row.createCell(1, STRING).setCellValue(activity.name)
            row.createCell(2, STRING).setCellValue(activity.date.toString())
            row.createCell(3, NUMERIC).setCellValue(activity.amount.toDouble())
            row.createCell(4, STRING).setCellValue(activity.currency)
        }
    }
}
