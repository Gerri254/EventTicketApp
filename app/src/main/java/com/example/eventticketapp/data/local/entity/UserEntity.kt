package com.example.eventticketapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.eventticketapp.data.model.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val email: String,
    val photoUrl: String?,
    val isOrganizer: Boolean
) {
    fun toUser(): User {
        return User(
            id = id,
            name = name,
            email = email,
            photoUrl = photoUrl,
            isOrganizer = isOrganizer
        )
    }

    companion object {
        fun fromUser(user: User): UserEntity {
            return UserEntity(
                id = user.id,
                name = user.name,
                email = user.email,
                photoUrl = user.photoUrl,
                isOrganizer = user.isOrganizer
            )
        }
    }
}