package com.example.eventticketapp.data.model

data class TicketType(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0,
    val availableQuantity: Int = 0,
    val eventId: String = ""
)