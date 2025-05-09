package com.example.eventticketapp.data.model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val isOrganizer: Boolean = false
)