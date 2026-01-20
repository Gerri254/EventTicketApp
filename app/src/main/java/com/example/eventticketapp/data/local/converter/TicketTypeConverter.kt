package com.example.eventticketapp.data.local.converter

import androidx.room.TypeConverter
import com.example.eventticketapp.data.model.TicketType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class TicketTypeConverter {
    @TypeConverter
    fun fromTicketTypeList(ticketTypes: List<TicketType>): String {
        return Json.encodeToString(ticketTypes)
    }

    @TypeConverter
    fun toTicketTypeList(ticketTypesString: String): List<TicketType> {
        return Json.decodeFromString(ticketTypesString)
    }
}