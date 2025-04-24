package com.example.eventticketapp.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.eventticketapp.data.model.Event
import com.example.eventticketapp.data.model.Ticket
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.Image
import com.itextpdf.text.PageSize
import com.itextpdf.text.Paragraph
import com.itextpdf.text.Rectangle
import com.itextpdf.text.pdf.PdfWriter
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
        val document = Document(PageSize.A5)
        PdfWriter.getInstance(document, FileOutputStream(pdfFile))

        document.open()

        // Add ticket content
        addTicketContent(document, ticket, event, qrCodeBitmap)

        document.close()

        // Return the URI of the file
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

    private fun addTicketContent(
        document: Document,
        ticket: Ticket,
        event: Event,
        qrCodeBitmap: Bitmap
    ) {
        // Define fonts
        val titleFont = Font(Font.FontFamily.HELVETICA, 18f, Font.BOLD)
        val headingFont = Font(Font.FontFamily.HELVETICA, 14f, Font.BOLD)
        val normalFont = Font(Font.FontFamily.HELVETICA, 12f, Font.NORMAL)

        // Add event title
        val titleParagraph = Paragraph(event.title, titleFont)
        titleParagraph.alignment = Element.ALIGN_CENTER
        titleParagraph.spacingAfter = 20f
        document.add(titleParagraph)

        // Add event details
        document.add(Paragraph("Date: ${dateTimeUtils.formatDate(event.date)}", normalFont))
        document.add(Paragraph("Time: ${dateTimeUtils.formatTime(event.date)}", normalFont))
        document.add(Paragraph("Location: ${event.location}", normalFont))
        document.add(Paragraph("Ticket ID: ${ticket.id}", normalFont))

        // Add spacing
        document.add(Paragraph(" ", normalFont))

        // Add QR code
        val qrImage = Image.getInstance(bitmapToByteArray(qrCodeBitmap))
        qrImage.scaleToFit(200f, 200f)
        qrImage.alignment = Element.ALIGN_CENTER
        document.add(qrImage)

        // Add footer
        val footerParagraph = Paragraph("Present this ticket at the entrance", normalFont)
        footerParagraph.alignment = Element.ALIGN_CENTER
        footerParagraph.spacingBefore = 20f
        document.add(footerParagraph)
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }
}