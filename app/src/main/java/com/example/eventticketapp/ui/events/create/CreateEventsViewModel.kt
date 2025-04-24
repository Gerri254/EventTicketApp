package com.example.eventticketapp.ui.events.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventticketapp.data.model.Event
import com.example.eventticketapp.data.model.Resource
import com.example.eventticketapp.data.model.TicketType
import com.example.eventticketapp.data.repository.EventRepository
import com.example.eventticketapp.data.repository.UserRepository
import com.example.eventticketapp.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CreateEventViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val userRepository: UserRepository,

) : ViewModel() {

    private val _createEventState = MutableStateFlow<Resource<String>?>(null)
    val createEventState: StateFlow<Resource<String>?> = _createEventState

    private val _imageUrl = MutableStateFlow<String?>(null)
    val imageUrl: StateFlow<String?> = _imageUrl

    private val _eventCategories = MutableStateFlow<List<String>>(Constants.EVENT_CATEGORIES)
    val eventCategories: StateFlow<List<String>> = _eventCategories

    // For editing an existing event
    private val _currentEvent = MutableStateFlow<Event?>(null)
    val currentEvent: StateFlow<Event?> = _currentEvent

    // For managing ticket types
    private val _ticketTypes = MutableStateFlow<List<TicketType>>(emptyList())
    val ticketTypes: StateFlow<List<TicketType>> = _ticketTypes

    fun loadEvent(eventId: String) {
        viewModelScope.launch {
            val event = eventRepository.getEventByIdFromRemote(eventId)
            _currentEvent.value = event
            event?.ticketTypes?.let {
                _ticketTypes.value = it
            }
        }
    }

    fun setImageUrl(url: String) {
        _imageUrl.value = url
    }

    fun createEvent(
        title: String,
        description: String,
        location: String,
        date: Date,
        category: String,
        isPublic: Boolean,
        imageUrl: String?
    ) {
        viewModelScope.launch {
            _createEventState.value = Resource.Loading()

            try {
                val userId = userRepository.getCurrentUserId()
                if (userId == null) {
                    _createEventState.value = Resource.Error("User not authenticated")
                    return@launch
                }

                val event = _currentEvent.value?.copy(
                    title = title,
                    description = description,
                    location = location,
                    date = date,
                    category = category,
                    isPublic = isPublic,
                    imageUrl = imageUrl ?: _currentEvent.value?.imageUrl,
                    updatedAt = Date()
                ) ?: Event(
                    id = "",
                    title = title,
                    description = description,
                    location = location,
                    date = date,
                    category = category,
                    organizerId = userId,
                    isPublic = isPublic,
                    imageUrl = imageUrl,
                    ticketTypes = emptyList()
                )

                val eventId = if (event.id.isEmpty()) {
                    eventRepository.createEventInRemote(event)
                } else {
                    eventRepository.updateEventInRemote(event)
                    event.id
                }

                _createEventState.value = Resource.Success(eventId)
            } catch (e: Exception) {
                _createEventState.value = Resource.Error(e.message ?: "Failed to create event")
            }
        }
    }

    fun addTicketType(
        name: String,
        description: String,
        price: Double,
        quantity: Int,
        eventId: String
    ) {
        val newTicketType = TicketType(
            id = UUID.randomUUID().toString(),
            name = name,
            description = description,
            price = price,
            quantity = quantity,
            availableQuantity = quantity,
            eventId = eventId
        )

        val currentTicketTypes = _ticketTypes.value.toMutableList()
        currentTicketTypes.add(newTicketType)
        _ticketTypes.value = currentTicketTypes
    }

    fun updateTicketType(
        ticketType: TicketType
    ) {
        val currentTicketTypes = _ticketTypes.value.toMutableList()
        val index = currentTicketTypes.indexOfFirst { it.id == ticketType.id }

        if (index != -1) {
            currentTicketTypes[index] = ticketType
            _ticketTypes.value = currentTicketTypes
        }
    }

    fun removeTicketType(ticketTypeId: String) {
        val currentTicketTypes = _ticketTypes.value.toMutableList()
        currentTicketTypes.removeAll { it.id == ticketTypeId }
        _ticketTypes.value = currentTicketTypes
    }

    fun saveTicketTypes(eventId: String) {
        viewModelScope.launch {
            try {
                val event = eventRepository.getEventByIdFromRemote(eventId)

                if (event != null) {
                    // Update event with new ticket types
                    val updatedEvent = event.copy(
                        ticketTypes = _ticketTypes.value,
                        updatedAt = Date()
                    )

                    eventRepository.updateEventInRemote(updatedEvent)
                    _currentEvent.value = updatedEvent
                    _createEventState.value = Resource.Success(eventId)
                } else {
                    _createEventState.value = Resource.Error("Event not found")
                }
            } catch (e: Exception) {
                _createEventState.value = Resource.Error(e.message ?: "Failed to save ticket types")
            }
        }
    }

    fun clearStates() {
        _createEventState.value = null

    }
}
