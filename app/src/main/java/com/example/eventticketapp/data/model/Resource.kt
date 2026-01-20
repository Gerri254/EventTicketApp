package com.example.eventticketapp.data.model

import kotlinx.serialization.Serializable

@Serializable
sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    @Serializable
    class Success<T>(val _data: T) : Resource<T>(_data)
    @Serializable
    class Error<T>(val _message: String, val _data: T? = null) : Resource<T>(_data, _message)
    @Serializable
    class Loading<T>(val _data: T? = null) : Resource<T>(_data)
}