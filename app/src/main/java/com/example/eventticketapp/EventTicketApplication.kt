package com.example.eventticketapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.eventticketapp.util.Constants
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class EventTicketApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PDFBoxResourceLoader.init(applicationContext)
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val eventRemindersChannel = NotificationChannel(
                Constants.CHANNEL_EVENT_REMINDERS,
                "Event Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders for upcoming events"
            }

            val ticketUpdatesChannel = NotificationChannel(
                Constants.CHANNEL_TICKET_UPDATES,
                "Ticket Updates",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Updates about your tickets"
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(eventRemindersChannel)
            notificationManager.createNotificationChannel(ticketUpdatesChannel)
        }
    }
}