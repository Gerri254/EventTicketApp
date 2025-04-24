package com.example.eventticketapp.data.model

import java.util.Date

data class Event(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val date: Date = Date(),
    val category: String = "",
    val organizerId: String = "",
    val isPublic: Boolean = true,
    val imageUrl: String? = null,
    val ticketTypes: List<TicketType> = emptyList(),
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)