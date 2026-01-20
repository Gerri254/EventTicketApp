package com.example.eventticketapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class TicketType(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0,
    val availableQuantity: Int = 0,
    val eventId: String = ""
)