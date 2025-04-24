package com.example.eventticketapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.eventticketapp.data.local.converter.DateConverter
import com.example.eventticketapp.data.model.Ticket
import java.util.Date

@Entity(tableName = "tickets")
@TypeConverters(DateConverter::class)
data class TicketEntity(
    @PrimaryKey
    val id: String,
    val ticketTypeId: String,
    val eventId: String,
    val userId: String,
    val qrCodeData: String,
    val isScanned: Boolean,
    val scannedAt: Date?,
    val purchasedAt: Date
) {
    fun toTicket(): Ticket {
        return Ticket(
            id = id,
            ticketTypeId = ticketTypeId,
            eventId = eventId,
            userId = userId,
            qrCodeData = qrCodeData,
            isScanned = isScanned,
            scannedAt = scannedAt,
            purchasedAt = purchasedAt
        )
    }

    companion object {
        fun fromTicket(ticket: Ticket): TicketEntity {
            return TicketEntity(
                id = ticket.id,
                ticketTypeId = ticket.ticketTypeId,
                eventId = ticket.eventId,
                userId = ticket.userId,
                qrCodeData = ticket.qrCodeData,
                isScanned = ticket.isScanned,
                scannedAt = ticket.scannedAt,
                purchasedAt = ticket.purchasedAt
            )
        }
    }
}