package com.example.eventticketapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.eventticketapp.data.local.converter.DateConverter
import com.example.eventticketapp.data.local.converter.TicketTypeConverter
import com.example.eventticketapp.data.model.Event
import com.example.eventticketapp.data.model.TicketType
import java.util.Date

@Entity(tableName = "events")
@TypeConverters(DateConverter::class, TicketTypeConverter::class)
data class EventEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val location: String,
    val date: Date,
    val category: String,
    val organizerId: String,
    val isPublic: Boolean,
    val imageUrl: String?,
    val ticketTypes: List<TicketType>,
    val createdAt: Date,
    val updatedAt: Date
) {
    fun toEvent(): Event {
        return Event(
            id = id,
            title = title,
            description = description,
            location = location,
            date = date,
            category = category,
            organizerId = organizerId,
            isPublic = isPublic,
            imageUrl = imageUrl,
            ticketTypes = ticketTypes,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromEvent(event: Event): EventEntity {
            return EventEntity(
                id = event.id,
                title = event.title,
                description = event.description,
                location = event.location,
                date = event.date,
                category = event.category,
                organizerId = event.organizerId,
                isPublic = event.isPublic,
                imageUrl = event.imageUrl,
                ticketTypes = event.ticketTypes,
                createdAt = event.createdAt,
                updatedAt = event.updatedAt
            )
        }
    }
}