package com.example.eventticketapp.data.model

import com.example.eventticketapp.util.DateSerializer
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class Ticket(
    val id: String = "",
    val ticketTypeId: String = "",
    val eventId: String = "",
    val userId: String = "",
    val qrCodeData: String = "",
    val isScanned: Boolean = false,
    @Serializable(with = DateSerializer::class)
    val scannedAt: Date? = null,
    @Serializable(with = DateSerializer::class)
    val purchasedAt: Date = Date()
)