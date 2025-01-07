package pl.edu.agh.gem.internal.job.stage

import com.itextpdf.text.BaseColor
import com.itextpdf.text.Document
import com.itextpdf.text.Element.ALIGN_CENTER
import com.itextpdf.text.Element.ALIGN_MIDDLE
import com.itextpdf.text.Font
import com.itextpdf.text.Font.BOLD
import com.itextpdf.text.Font.FontFamily.HELVETICA
import com.itextpdf.text.Font.FontFamily.TIMES_ROMAN
import com.itextpdf.text.Font.NORMAL
import com.itextpdf.text.Image
import com.itextpdf.text.Paragraph
import com.itextpdf.text.Phrase
import com.itextpdf.text.Rectangle.NO_BORDER
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import com.itextpdf.text.pdf.PdfWriter.getInstance
import com.itextpdf.text.pdf.draw.LineSeparator
import org.bson.types.Binary
import org.springframework.stereotype.Component
import pl.edu.agh.gem.internal.client.AttachmentStoreClient
import pl.edu.agh.gem.internal.model.finance.GroupActivities
import pl.edu.agh.gem.internal.model.finance.GroupBalances
import pl.edu.agh.gem.internal.model.finance.GroupSettlements
import pl.edu.agh.gem.internal.model.group.GroupDetails
import pl.edu.agh.gem.internal.model.user.UsersDetails
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.math.BigDecimal
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Component
class GeneratePDFReportStage(
    private val attachmentStoreClient: AttachmentStoreClient,
) : GenerateReportStage() {
    override fun generateReport(
        balances: List<GroupBalances>,
        settlements: List<GroupSettlements>,
        activities: List<GroupActivities>,
        usersDetails: UsersDetails,
        groupDetails: GroupDetails,
    ): Binary {
        val document = Document()
        val outputStream = ByteArrayOutputStream()
        val writer = getInstance(document, outputStream)
        document.open()

        document.addSummary(activities, groupDetails, writer)
        document.newPage()

        activities.forEach { groupActivity ->
            document.addActivities(groupActivity, usersDetails)
            document.newPage()
        }

        balances.forEach { balance ->
            document.addBalances(balance, usersDetails)
            document.newPage()
        }

        settlements.forEach { groupSettlement ->
            document.addSettlements(groupSettlement, usersDetails)
            document.newPage()
        }

        document.close()
        saveToFile(outputStream)

        return outputStream.toByteArray().let(::Binary)
    }

    private fun saveToFile(outputStream: ByteArrayOutputStream) {
        try {
            val file = File("src/main/resources/debug_report.pdf")
            val fileOutputStream = FileOutputStream(file)
            outputStream.writeTo(fileOutputStream)
            fileOutputStream.close()
            println("PDF report saved to: ${file.absolutePath}")
        } catch (e: Exception) {
            println("Error saving PDF report: ${e.message}")
        }
    }

    private fun Document.addSummary(
        activities: List<GroupActivities>,
        groupDetails: GroupDetails,
        writer: PdfWriter,
    ) {
        val startDate = activities.flatMap { it.activities }.minOfOrNull { it.date }
        val endDate = activities.flatMap { it.activities }.maxOfOrNull { it.date }

        val formattedStartDate = startDate?.let { dateFormatter.format(it) }
        val formattedEndDate = endDate?.let { dateFormatter.format(it) }

        val groupImage: ByteArray = attachmentStoreClient.getAttachment(groupDetails.groupId, groupDetails.attachmentId)
        val logoImage = loadImageFromResources("/images/logo.png")
        addCircularImage(groupImage, writer, isLogo = false)
        addCircularImage(logoImage, writer, isLogo = true)

        repeat(NUM_OF_EMPTY_PARAGRAPHS_IN_SUMMARY) { add(Paragraph(" ")) }

        val title =
            Paragraph("Activities Summary for group ${groupDetails.name}", titleFont).apply {
                alignment = ALIGN_CENTER
            }

        add(title)

        val subtitle =
            Paragraph("Report generated from $formattedStartDate to $formattedEndDate", subHeaderFont).apply {
                alignment = ALIGN_CENTER
            }
        add(subtitle)

        add(Paragraph(" "))

        addSeparatorLine()

        val table = PdfPTable(NUM_OF_COLUMNS_IN_SUMMARY_TABLE)
        table.widthPercentage = TABLE_WIDTH_PERCENTAGE
        table.setWidths(SUMMARY_TABLE_WIDTHS)
        table.headerRows = NUM_OF_HEADER_ROWS_IN_TABLE
        table.spacingBefore = TABLE_SPACING_BEFORE
        table.spacingAfter = TABLE_SPACING_AFTER

        table.addCell(createCell("Type", tableHeaderFont, primaryColorBase))
        table.addCell(createCell("Status", tableHeaderFont, primaryColorBase))
        table.addCell(createCell("Currency", tableHeaderFont, primaryColorBase))
        table.addCell(createCell("Count", tableHeaderFont, primaryColorBase))
        table.addCell(createCell("Total Amount", tableHeaderFont, primaryColorBase))

        val groupedActivities =
            activities.flatMap { groupActivity ->
                groupActivity.activities.map { activity ->
                    Triple(activity.type, activity.status, groupActivity.currency) to activity.value
                }
            }.groupBy({ it.first }) { it.second }

        groupedActivities.forEach { (typeStatusCurrency, values) ->
            val count = values.size
            val totalAmount = values.reduce(BigDecimal::add)
            val (type, status, currency) = typeStatusCurrency

            val cellColor = if (table.rows.size % 2 == 0) primaryColorLightest else whiteColorLightest
            table.addCell(createCell(type.toString(), backgroundColor = cellColor))
            table.addCell(createCell(status.toString(), backgroundColor = cellColor))
            table.addCell(createCell(currency, backgroundColor = cellColor))
            table.addCell(createCell(count.toString(), backgroundColor = cellColor))
            table.addCell(createCell(totalAmount.toString(), backgroundColor = cellColor))
        }

        add(Paragraph("Activities Summary by Type, Status, and Currency", subHeaderFont))
        add(table)
    }

    private fun addCircularImage(
        imageBytes: ByteArray,
        writer: PdfWriter,
        isLogo: Boolean,
    ) {
        val canvas = writer.directContent
        val x = if (isLogo) LOGO_X_COORDINATE else GROUP_IMAGE_X_COORDINATE
        val y = IMAGE_Y_COORDINATE
        val radius = IMAGE_RADIUS
        val borderWidth = IMAGE_BORDER_WIDTH

        canvas.setColorStroke(whiteColorLightest)
        canvas.setLineWidth(borderWidth)
        canvas.circle(x + radius, y + radius, radius)
        canvas.stroke()

        val mask = canvas.createTemplate(radius * 2, radius * 2)
        mask.circle(radius, radius, radius)
        mask.clip()
        mask.newPath()

        val image = Image.getInstance(imageBytes)
        image.scaleToFit(radius * 2, radius * 2)
        image.setAbsolutePosition(IMAGE_ABSOLUTE_X_POSITION, IMAGE_ABSOLUTE_Y_POSITION)
        mask.addImage(image)

        canvas.addTemplate(mask, x, y)
    }

    private fun loadImageFromResources(path: String): ByteArray {
        val inputStream = this.javaClass.getResourceAsStream(path)
        return inputStream?.readBytes() ?: throw ImageNotFoundException("Image not found in resources: $path")
    }

    private fun Document.addSeparatorLine() {
        val separator =
            LineSeparator(SEPARATOR_LINE_WIDTH, SEPARATOR_LINE_PERCENTAGE, primaryColorBase, LineSeparator.ALIGN_CENTER, SEPARATOR_LINE_OFFSET)
        add(separator)
    }

    private fun createCell(
        content: String,
        font: Font = bodyFont,
        backgroundColor: BaseColor = whiteColorLightest,
    ): PdfPCell {
        return PdfPCell(Phrase(content, font)).apply {
            this.backgroundColor = backgroundColor
            horizontalAlignment = ALIGN_CENTER
            verticalAlignment = ALIGN_MIDDLE
            border = NO_BORDER
            borderWidth = CELL_BORDER_WIDTH
            borderWidthBottom = CELL_BORDER_BOTTOM_WIDTH
        }
    }

    private fun Document.addActivities(
        groupActivity: GroupActivities,
        usersDetails: UsersDetails,
    ) {
        addSeparatorLine()
        add(Paragraph("Activity Report for ${groupActivity.currency}", Font(HELVETICA, ACTIVITY_REPORT_FONT_SIZE, BOLD, primaryColorBase)))
        add(Paragraph(" "))

        val table = PdfPTable(NUM_OF_COLUMNS_IN_ACTIVITY_TABLE)
        table.widthPercentage = TABLE_WIDTH_PERCENTAGE
        table.setWidths(ACTIVITY_TABLE_WIDTHS)
        table.headerRows = NUM_OF_HEADER_ROWS_IN_TABLE
        table.spacingBefore = TABLE_SPACING_BEFORE
        table.spacingAfter = TABLE_SPACING_AFTER

        table.addCell(createCell("Activity Type", tableHeaderFont, primaryColorBase))
        table.addCell(createCell("Status", tableHeaderFont, primaryColorBase))
        table.addCell(createCell("User", tableHeaderFont, primaryColorBase))
        table.addCell(createCell("Amount", tableHeaderFont, primaryColorBase))

        groupActivity.activities.forEach { activity ->
            val cellColor = if (table.rows.size % 2 == 0) primaryColorLightest else whiteColorLightest
            table.addCell(createCell(activity.type.toString(), backgroundColor = cellColor))
            table.addCell(createCell(activity.status.toString(), backgroundColor = cellColor))
            table.addCell(createCell(usersDetails.getUserName(activity.creatorId), backgroundColor = cellColor))
            table.addCell(createCell(activity.value.toString(), backgroundColor = cellColor))
        }

        add(table)
        add(Paragraph(" "))
    }

    private fun Document.addBalances(
        balances: GroupBalances,
        usersDetails: UsersDetails,
    ) {
        addSeparatorLine()
        add(Paragraph("Balances - ${balances.currency}", Font(HELVETICA, BALANCES_REPORT_FONT_SIZE, BOLD, primaryColorBase)))
        add(Paragraph(" "))

        val table = PdfPTable(NUM_OF_COLUMNS_IN_BALANCE_TABLE)
        table.widthPercentage = TABLE_WIDTH_PERCENTAGE
        table.setWidths(BALANCE_TABLE_WIDTHS)
        table.headerRows = NUM_OF_HEADER_ROWS_IN_TABLE
        table.spacingBefore = TABLE_SPACING_BEFORE
        table.spacingAfter = TABLE_SPACING_AFTER

        table.addCell(createCell("User name", tableHeaderFont, primaryColorBase))
        table.addCell(createCell("Balance", tableHeaderFont, primaryColorBase))
        table.addCell(createCell("Currency", tableHeaderFont, primaryColorBase))

        balances.balances.forEach { user ->
            val cellColor = if (table.rows.size % 2 == 0) primaryColorLightest else whiteColorLightest
            table.addCell(createCell(usersDetails.getUserName(user.userId), backgroundColor = cellColor))
            table.addCell(createCell(user.value.toString(), backgroundColor = cellColor))
            table.addCell(createCell(balances.currency, backgroundColor = cellColor))
        }

        add(table)
        add(Paragraph(" "))
    }

    private fun Document.addSettlements(
        groupSettlements: GroupSettlements,
        usersDetails: UsersDetails,
    ) {
        addSeparatorLine()
        add(Paragraph("Settlements - ${groupSettlements.currency}", Font(HELVETICA, SETTLEMENTS_REPORT_FONT_SIZE, BOLD, primaryColorBase)))
        add(Paragraph(" "))

        val table = PdfPTable(NUM_OF_COLUMNS_IN_SETTLEMENTS_TABLE)
        table.widthPercentage = TABLE_WIDTH_PERCENTAGE
        table.setWidths(SETTLEMENTS_TABLE_WIDTHS)
        table.headerRows = NUM_OF_HEADER_ROWS_IN_TABLE
        table.spacingBefore = TABLE_SPACING_BEFORE
        table.spacingAfter = TABLE_SPACING_AFTER

        table.addCell(createCell("User From", tableHeaderFont, primaryColorBase))
        table.addCell(createCell("User To", tableHeaderFont, primaryColorBase))
        table.addCell(createCell("Amount", tableHeaderFont, primaryColorBase))
        table.addCell(createCell("Currency", tableHeaderFont, primaryColorBase))

        groupSettlements.settlements.forEach { settlement ->
            val cellColor = if (table.rows.size % 2 == 0) primaryColorLightest else whiteColorLightest
            table.addCell(createCell(usersDetails.getUserName(settlement.fromUserId), backgroundColor = cellColor))
            table.addCell(createCell(usersDetails.getUserName(settlement.toUserId), backgroundColor = cellColor))
            table.addCell(createCell(settlement.value.toString(), backgroundColor = cellColor))
            table.addCell(createCell(groupSettlements.currency, backgroundColor = cellColor))
        }

        add(table)
        add(Paragraph(" "))
    }

    companion object {
        val primaryColorBase = BaseColor(30, 167, 255)
        val primaryColorLightest = BaseColor(214, 241, 255)
        val whiteColorLightest = BaseColor(247, 249, 250)
        val blackColorBase = BaseColor(64, 68, 70)
        val subHeaderFont = Font(HELVETICA, 12f, NORMAL, blackColorBase)
        val bodyFont = Font(HELVETICA, 10f, NORMAL, blackColorBase)
        val titleFont = Font(TIMES_ROMAN, 24f, BOLD, primaryColorBase)
        val tableHeaderFont = Font(HELVETICA, 12f, BOLD, whiteColorLightest)

        const val TABLE_WIDTH_PERCENTAGE = 100f
        const val TABLE_SPACING_BEFORE = 20f
        const val TABLE_SPACING_AFTER = 20f

        const val NUM_OF_EMPTY_PARAGRAPHS_IN_SUMMARY = 5
        const val NUM_OF_COLUMNS_IN_SUMMARY_TABLE = 5
        const val NUM_OF_COLUMNS_IN_ACTIVITY_TABLE = 4
        const val NUM_OF_COLUMNS_IN_BALANCE_TABLE = 3
        const val NUM_OF_COLUMNS_IN_SETTLEMENTS_TABLE = 4
        const val NUM_OF_HEADER_ROWS_IN_TABLE = 1

        const val LOGO_X_COORDINATE = 465f
        const val GROUP_IMAGE_X_COORDINATE = 65f
        const val IMAGE_Y_COORDINATE = 750f
        const val IMAGE_RADIUS = 32f
        const val IMAGE_BORDER_WIDTH = 2f
        const val IMAGE_ABSOLUTE_X_POSITION = 0f
        const val IMAGE_ABSOLUTE_Y_POSITION = 0f

        const val SEPARATOR_LINE_WIDTH = 1f
        const val SEPARATOR_LINE_PERCENTAGE = 100f
        const val SEPARATOR_LINE_OFFSET = -1f

        const val CELL_BORDER_WIDTH = 0f
        const val CELL_BORDER_BOTTOM_WIDTH = 1f

        const val ACTIVITY_REPORT_FONT_SIZE = 18f
        const val BALANCES_REPORT_FONT_SIZE = 18f
        const val SETTLEMENTS_REPORT_FONT_SIZE = 18f

        val dateFormatter =
            DateTimeFormatter.ofPattern("dd MMM YYYY HH:mm")
                .withZone(ZoneId.systemDefault())

        val SUMMARY_TABLE_WIDTHS = floatArrayOf(2f, 1.5f, 1.5f, 1f, 2f)
        val ACTIVITY_TABLE_WIDTHS = floatArrayOf(3f, 2f, 2f, 2f)
        val BALANCE_TABLE_WIDTHS = floatArrayOf(3f, 2f, 2f)
        val SETTLEMENTS_TABLE_WIDTHS = floatArrayOf(2f, 2f, 2f, 2f)
    }
}

class ImageNotFoundException(override val message: String?) : RuntimeException()
