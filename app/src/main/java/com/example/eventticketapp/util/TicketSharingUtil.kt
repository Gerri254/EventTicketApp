package com.example.eventticketapp.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.example.eventticketapp.data.model.Event
import com.example.eventticketapp.data.model.Ticket
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TicketSharingUtil @Inject constructor(
    private val pdfExporter: PDFExporter,
    private val qrGeneratorUtil: QRGeneratorUtil,
    private val dateTimeUtils: DateTimeUtils
) {
    fun shareTicketPdf(
        context: Context,
        ticket: Ticket,
        event: Event,
        pdfUri: Uri
    ): Intent {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, pdfUri)
            putExtra(Intent.EXTRA_SUBJECT, "Ticket for ${event.title}")
            putExtra(
                Intent.EXTRA_TEXT,
                "Please find attached your ticket for ${event.title} on " +
                        "${dateTimeUtils.formatDate(event.date)} at ${dateTimeUtils.formatTime(event.date)}.\n" +
                        "Location: ${event.location}"
            )
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        return Intent.createChooser(shareIntent, "Share Ticket")
    }

    fun shareTicketText(context: Context, ticket: Ticket, event: Event): Intent {
        val shareText = buildString {
            append("My Ticket for ${event.title}\n\n")
            append("Date: ${dateTimeUtils.formatDate(event.date)}\n")
            append("Time: ${dateTimeUtils.formatTime(event.date)}\n")
            append("Location: ${event.location}\n\n")
            append("Ticket ID: ${ticket.id}\n")
            append("Please bring this ticket or scan the QR code at the entrance.")
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Ticket for ${event.title}")
            putExtra(Intent.EXTRA_TEXT, shareText)
        }

        return Intent.createChooser(shareIntent, "Share Ticket")
    }
}