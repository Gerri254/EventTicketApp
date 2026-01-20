package com.example.eventticketapp.data.model

import com.example.eventticketapp.util.DateSerializer
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class Event(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val location: String = "",
    @Serializable(with = DateSerializer::class)
    val date: Date = Date(),
    val category: String = "",
    val organizerId: String = "",
    val isPublic: Boolean = true,
    val imageUrl: String? = null,
    val ticketTypes: List<TicketType> = emptyList(),
    @Serializable(with = DateSerializer::class)
    val createdAt: Date = Date(),
    @Serializable(with = DateSerializer::class)
    val updatedAt: Date = Date()
)