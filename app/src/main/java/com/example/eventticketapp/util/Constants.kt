package com.example.eventticketapp.util

object Constants {
    // Firebase collections
    const val COLLECTION_USERS = "users"
    const val COLLECTION_EVENTS = "events"
    const val COLLECTION_TICKETS = "tickets"

    // Shared Preferences
    const val PREF_NAME = "event_ticket_prefs"
    const val PREF_USER_ID = "user_id"
    const val PREF_USER_NAME = "user_name"
    const val PREF_USER_EMAIL = "user_email"
    const val PREF_IS_LOGGED_IN = "is_logged_in"
    const val PREF_IS_ORGANIZER = "is_organizer"

    // Event categories
    val EVENT_CATEGORIES = listOf(
        "Concert",
        "Conference",
        "Exhibition",
        "Festival",
        "Meeting",
        "Party",
        "Seminar",
        "Sports",
        "Theatre",
        "Wedding",
        "Workshop",
        "Other"
    )

    // Ticket types
    const val TICKET_TYPE_VIP = "VIP"
    const val TICKET_TYPE_REGULAR = "Regular"
    const val TICKET_TYPE_FREE = "Free"

    // Intent extras
    const val EXTRA_EVENT_ID = "event_id"
    const val EXTRA_TICKET_ID = "ticket_id"
    const val EXTRA_IS_ORGANIZER = "is_organizer"

    // Request codes
    const val RC_SIGN_IN = 9001
    const val RC_CAMERA_PERMISSION = 9002
    const val RC_SCAN_QR = 9003
    const val RC_PICK_IMAGE = 9004

    // Notification channels
    const val CHANNEL_EVENT_REMINDERS = "event_reminders"
    const val CHANNEL_TICKET_UPDATES = "ticket_updates"
}