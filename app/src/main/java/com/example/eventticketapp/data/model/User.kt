package com.example.eventticketapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val isOrganizer: Boolean = false
)