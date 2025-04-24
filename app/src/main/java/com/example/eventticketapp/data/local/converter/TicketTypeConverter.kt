package com.example.eventticketapp.data.local.converter

import androidx.room.TypeConverter
import com.example.eventticketapp.data.model.TicketType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TicketTypeConverter {
    @TypeConverter
    fun fromTicketTypeList(ticketTypes: List<TicketType>): String {
        val gson = Gson()
        return gson.toJson(ticketTypes)
    }

    @TypeConverter
    fun toTicketTypeList(ticketTypesString: String): List<TicketType> {
        val gson = Gson()
        val type = object : TypeToken<List<TicketType>>() {}.type
        return gson.fromJson(ticketTypesString, type)
    }
}