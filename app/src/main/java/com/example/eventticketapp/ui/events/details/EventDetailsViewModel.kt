package com.example.eventticketapp.ui.events.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventticketapp.data.model.Event
import com.example.eventticketapp.data.model.Resource
import com.example.eventticketapp.data.model.Ticket
import com.example.eventticketapp.data.model.TicketType
import com.example.eventticketapp.data.repository.EventRepository
import com.example.eventticketapp.data.repository.TicketRepository
import com.example.eventticketapp.data.repository.UserRepository
import com.example.eventticketapp.util.QRGeneratorUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class EventDetailsViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val ticketRepository: TicketRepository,
    private val userRepository: UserRepository,
    private val qrGeneratorUtil: QRGeneratorUtil
) : ViewModel() {

    private val _event = MutableStateFlow<Resource<Event>>(Resource.Loading())
    val event: StateFlow<Resource<Event>> = _event

    private val _registerState = MutableStateFlow<Resource<Ticket>?>(null)
    val registerState: StateFlow<Resource<Ticket>?> = _registerState

    private val _isUserRegistered = MutableStateFlow<Boolean>(false)
    val isUserRegistered: StateFlow<Boolean> = _isUserRegistered

    private val _selectedTicketType = MutableStateFlow<TicketType?>(null)
    val selectedTicketType: StateFlow<TicketType?> = _selectedTicketType

    fun loadEvent(eventId: String) {
        viewModelScope.launch {
            try {
                val event = eventRepository.getEventByIdFromRemote(eventId)

                if (event != null) {
                    _event.value = Resource.Success(event)

                    // Check if user is already registered
                    checkIfUserRegistered(eventId)

                    // Set default selected ticket type (first one)
                    if (event.ticketTypes.isNotEmpty()) {
                        _selectedTicketType.value = event.ticketTypes.first()
                    }
                } else {
                    _event.value = Resource.Error("Event not found")
                }
            } catch (e: Exception) {
                _event.value = Resource.Error(e.message ?: "Failed to load event")
            }
        }
    }

    private suspend fun checkIfUserRegistered(eventId: String) {
        val userId = userRepository.getCurrentUserId() ?: return

        try {
            val userTickets = ticketRepository.getUserTicketsFromRemote(userId)
                .collect { resource ->
                    if (resource is Resource.Success) {
                        val tickets = resource.data ?: emptyList()
                        _isUserRegistered.value = tickets.any { it.eventId == eventId }
                    }
                }
        } catch (e: Exception) {
            // Handle error
        }
    }

    fun selectTicketType(ticketType: TicketType) {
        _selectedTicketType.value = ticketType
    }

    fun registerForEvent() {
        viewModelScope.launch {
            _registerState.value = Resource.Loading()

            try {
                val currentEvent = (event.value as? Resource.Success)?.data
                val ticketType = selectedTicketType.value
                val userId = userRepository.getCurrentUserId()

                if (currentEvent == null || ticketType == null || userId == null) {
                    _registerState.value = Resource.Error("Missing required data")
                    return@launch
                }

                // Check if tickets are available
                if (ticketType.availableQuantity <= 0) {
                    _registerState.value = Resource.Error("No tickets available")
                    return@launch
                }

                // Create a unique QR code data
                val ticketId = UUID.randomUUID().toString()
                val qrCodeData = qrGeneratorUtil.generateTicketQRCodeData(
                    ticketId = ticketId,
                    eventId = currentEvent.id,
                    userId = userId
                )

                // Create ticket
                val ticket = Ticket(
                    id = ticketId,
                    ticketTypeId = ticketType.id,
                    eventId = currentEvent.id,
                    userId = userId,
                    qrCodeData = qrCodeData,
                    isScanned = false,
                    scannedAt = null,
                    purchasedAt = Date()
                )

                // Save ticket
                val savedTicketId = ticketRepository.createTicketInRemote(ticket)

                if (savedTicketId.isNotEmpty()) {
                    // Update available ticket count
                    val updatedTicketType = ticketType.copy(
                        availableQuantity = ticketType.availableQuantity - 1
                    )

                    val updatedTicketTypes = currentEvent.ticketTypes.map {
                        if (it.id == updatedTicketType.id) updatedTicketType else it
                    }

                    val updatedEvent = currentEvent.copy(
                        ticketTypes = updatedTicketTypes,
                        updatedAt = Date()
                    )

                    eventRepository.updateEventInRemote(updatedEvent)

                    // Update state
                    _registerState.value = Resource.Success(ticket)
                    _isUserRegistered.value = true
                    _event.value = Resource.Success(updatedEvent)
                } else {
                    _registerState.value = Resource.Error("Failed to register for event")
                }
            } catch (e: Exception) {
                _registerState.value = Resource.Error(e.message ?: "Failed to register for event")
            }
        }
    }

    fun clearRegistrationState() {
        _registerState.value = null
    }

    fun isUserOrganizer(): Boolean {
        val userId = userRepository.getCurrentUserId() ?: return false
        val currentEvent = (event.value as? Resource.Success)?.data ?: return false

        return currentEvent.organizerId == userId
    }
}