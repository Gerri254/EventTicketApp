package com.example.eventticketapp.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.eventticketapp.data.model.Event
import com.example.eventticketapp.data.model.Ticket
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PDFExporter @Inject constructor(
    private val dateTimeUtils: DateTimeUtils
) {
    fun exportTicketToPdf(
        context: Context,
        ticket: Ticket,
        event: Event,
        qrCodeBitmap: Bitmap
    ): Uri {
        val pdfFile = createPdfFile(context, "Ticket_${ticket.id}.pdf")
        val document = PDDocument()

        try {
            val page = PDPage(PDRectangle.A5)
            document.addPage(page)

            val contentStream = PDPageContentStream(document, page)

            // Dimensions
            val pageWidth = page.mediaBox.width
            val pageHeight = page.mediaBox.height
            val margin = 50f
            var yPosition = pageHeight - margin

            // Title
            contentStream.beginText()
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18f)
            contentStream.newLineAtOffset(margin, yPosition)
            contentStream.showText(event.title)
            contentStream.endText()
            yPosition -= 30f

            // Details
            val details = listOf(
                "Date: ${dateTimeUtils.formatDate(event.date)}",
                "Time: ${dateTimeUtils.formatTime(event.date)}",
                "Location: ${event.location}",
                "Ticket ID: ${ticket.id}"
            )

            contentStream.beginText()
            contentStream.setFont(PDType1Font.HELVETICA, 12f)
            // Need to reset offset because endText() was called
            contentStream.newLineAtOffset(margin, yPosition)

            details.forEach { detail ->
                contentStream.showText(detail)
                contentStream.newLineAtOffset(0f, -20f)
                yPosition -= 20f
            }
            contentStream.endText()

            // QR Code
            val stream = ByteArrayOutputStream()
            qrCodeBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val byteArray = stream.toByteArray()
            val pdImage = PDImageXObject.createFromByteArray(document, byteArray, "qr")

            val qrSize = 200f
            val qrX = (pageWidth - qrSize) / 2
            val qrY = yPosition - qrSize - 20f

            contentStream.drawImage(pdImage, qrX, qrY, qrSize, qrSize)

            // Footer
            yPosition = qrY - 30f
            contentStream.beginText()
            contentStream.setFont(PDType1Font.HELVETICA, 12f)
            val footerText = "Present this ticket at the entrance"
            // Simple centering calculation (approximate width for font size 12)
            val textWidth = PDType1Font.HELVETICA.getStringWidth(footerText) / 1000 * 12
            val footerX = (pageWidth - textWidth) / 2
            contentStream.newLineAtOffset(footerX, yPosition)
            contentStream.showText(footerText)
            contentStream.endText()

            contentStream.close()

            document.save(pdfFile)
        } finally {
            document.close()
        }

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
    }

    private fun createPdfFile(context: Context, fileName: String): File {
        val directory = File(context.filesDir, "tickets")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        return File(directory, fileName)
    }
}