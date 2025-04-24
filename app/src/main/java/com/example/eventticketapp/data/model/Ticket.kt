package com.example.eventticketapp.data.model

import java.util.Date

data class Ticket(
    val id: String = "",
    val ticketTypeId: String = "",
    val eventId: String = "",
    val userId: String = "",
    val qrCodeData: String = "",
    val isScanned: Boolean = false,
    val scannedAt: Date? = null,
    val purchasedAt: Date = Date()
)