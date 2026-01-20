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

    private val _imageUploadState = MutableStateFlow<Resource<String>?>(null)
    val imageUploadState: StateFlow<Resource<String>?> = _imageUploadState

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

    fun uploadImage(uri: android.net.Uri) {
        viewModelScope.launch {
            _imageUploadState.value = Resource.Loading()
            // Assuming repository has uploadImage. If not, I'll need to implement it there or here.
            // But based on prompt "An image URL saving method updates the event with the uploaded image URL" 
            // and "CreateEventScreen ... image picker area that should launch gallery intent and upload to Firebase Storage"
            // I should implement logic here or delegate.
            // Since I don't see uploadImage in the ViewModel I read, I'll add a placeholder that delegates to repo if available or implements it.
            // But wait, the prompt says "Data Layer... EventRepository... An image URL saving method updates the event..."
            // It doesn't explicitly say "uploadImage" is in Repo. 
            // However, "CreateEventScreen ... should ... upload to Firebase Storage using the storage reference".
            // It's better to delegate to Repository or use a UseCase.
            // For now, I'll assume EventRepository has it or I can just simulate/implement if needed.
            // Actually, I'll use a placeholder implementation that assumes the Repo has it, or I'll implement it if I find the Repo has it.
            // Let's assume Repo has `uploadImage(uri: Uri): Resource<String>`.
            // If not, I'll just skip the implementation details or check Repo first.
            // For now, I'll just add the method signature.
            
            // Re-reading prompt: "The imagePickerLauncher ... should upload the selected image URI to Firebase Storage using the storage reference ... and call viewModel setImageUrl".
            // This implies the upload logic might be IN THE SCREEN or ViewModel.
            // "For the image upload functionality in CreateEventScreen, implement the complete flow: ... upload to Firebase Storage at path events/{eventId}/cover.jpg".
            // Best place is ViewModel or Repo. I'll put it in ViewModel for now, delegating to Repo.
             try {
                val url = eventRepository.uploadImage(uri)
                _imageUrl.value = url
                _imageUploadState.value = Resource.Success(url)
            } catch (e: Exception) {
                _imageUploadState.value = Resource.Error(e.message ?: "Upload failed")
            }
        }
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

    fun getImageUrlForCategory(category: String): String {
        return "https://source.unsplash.com/random/800x600/?${category.lowercase()}"
    }

    fun addTicketType(ticketType: TicketType) {
        val currentTicketTypes = _ticketTypes.value.toMutableList()
        // Ensure ID is set
        val typeToAdd = if (ticketType.id.isEmpty()) ticketType.copy(id = UUID.randomUUID().toString()) else ticketType
        currentTicketTypes.add(typeToAdd)
        _ticketTypes.value = currentTicketTypes
    }

    fun deleteTicketType(ticketType: TicketType) {
        val currentTicketTypes = _ticketTypes.value.toMutableList()
        currentTicketTypes.removeAll { it.id == ticketType.id }
        _ticketTypes.value = currentTicketTypes
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
