package com.example.eventticketapp.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DateTimeUtils @Inject constructor() {
    private val dateFormat = SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    private val datetimeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

    fun formatDate(date: Date): String {
        return dateFormat.format(date)
    }

    fun formatTime(date: Date): String {
        return timeFormat.format(date)
    }

    fun formatDateTime(date: Date): String {
        return datetimeFormat.format(date)
    }

    fun parseDate(dateString: String): Date? {
        return try {
            dateFormat.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }

    fun parseDateTime(dateTimeString: String): Date? {
        return try {
            datetimeFormat.parse(dateTimeString)
        } catch (e: Exception) {
            null
        }
    }

    fun getCurrentDate(): Date {
        return Date()
    }

    fun isEventUpcoming(eventDate: Date): Boolean {
        return eventDate.after(Date())
    }

    fun getDaysUntilEvent(eventDate: Date): Int {
        val diffInMillis = eventDate.time - Date().time
        return (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
    }
}