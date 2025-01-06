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

        repeat(5) { add(Paragraph(" ")) }

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

        val table = PdfPTable(5)
        table.widthPercentage = TABLE_WIDTH_PERCENTAGE
        table.setWidths(floatArrayOf(2f, 1.5f, 1.5f, 1f, 2f))
        table.headerRows = 1
        table.spacingBefore = TABLE_SPACING_BEFORE
        table.spacingAfter = TABLE_SPACING_AFTER

        table.addCell(createCell("Type", tableHeaderFont, primaryColorBase))
        table.addCell(createCell("Status", tableHeaderFont, primaryColorBase))
        table.addCell(createCell("Currency", tableHeaderFont, primaryColorBase))
        table.addCell(createCell("Count", tableHeaderFont, primaryColorBase))
        table.addCell(createCell("Total Amount", tableHeaderFont, primaryColorBase))

        activities.flatMap { groupActivity ->
            groupActivity.activities.map { activity ->
                Triple(activity.type, activity.status, groupActivity.currency) to activity.value
            }
        }.groupBy({ it.first }) { it.second }
            .forEach { (typeStatusCurrency, values) ->
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
        val x = if (isLogo) 465f else 65f
        val y = 750f
        val radius = 32f
        val borderWidth = 2f

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
        image.setAbsolutePosition(0f, 0f)
        mask.addImage(image)

        canvas.addTemplate(mask, x, y)
    }

    private fun loadImageFromResources(path: String): ByteArray {
        val inputStream = this.javaClass.getResourceAsStream(path)
        return inputStream!!.readBytes()
    }

    private fun Document.addSeparatorLine() {
        val separator = LineSeparator(1f, 100f, primaryColorBase, LineSeparator.ALIGN_CENTER, -1f)
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
            borderWidth = 0f
            borderWidthBottom = 1f
        }
    }

    private fun Document.addActivities(
        groupActivity: GroupActivities,
        usersDetails: UsersDetails,
    ) {
        addSeparatorLine()
        add(Paragraph("Activity Report for ${groupActivity.currency}", Font(HELVETICA, 18f, BOLD, primaryColorBase)))
        add(Paragraph(" "))

        val table = PdfPTable(4)
        table.widthPercentage = TABLE_WIDTH_PERCENTAGE
        table.setWidths(floatArrayOf(3f, 2f, 2f, 2f))
        table.headerRows = 1
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
        add(Paragraph("Balances - ${balances.currency}", Font(HELVETICA, 18f, BOLD, primaryColorBase)))
        add(Paragraph(" "))

        val table = PdfPTable(3)
        table.widthPercentage = TABLE_WIDTH_PERCENTAGE
        table.setWidths(floatArrayOf(3f, 2f, 2f))
        table.headerRows = 1
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
        add(Paragraph("Settlements - ${groupSettlements.currency}", Font(HELVETICA, 18f, BOLD, primaryColorBase)))
        add(Paragraph(" "))

        val table = PdfPTable(4)
        table.widthPercentage = TABLE_WIDTH_PERCENTAGE
        table.setWidths(floatArrayOf(2f, 2f, 2f, 2f))
        table.headerRows = 1
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

        val dateFormatter =
            DateTimeFormatter.ofPattern("dd MMM YYYY HH:mm")
                .withZone(ZoneId.systemDefault())
    }
}
