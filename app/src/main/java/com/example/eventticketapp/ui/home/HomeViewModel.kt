package com.example.eventticketapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventticketapp.data.model.Event
import com.example.eventticketapp.data.model.Resource
import com.example.eventticketapp.data.model.Ticket
import com.example.eventticketapp.data.model.User
import com.example.eventticketapp.data.repository.EventRepository
import com.example.eventticketapp.data.repository.TicketRepository
import com.example.eventticketapp.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val eventRepository: EventRepository,
    private val ticketRepository: TicketRepository
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _userEvents = MutableStateFlow<Resource<List<Event>>>(Resource.Loading())
    val userEvents: StateFlow<Resource<List<Event>>> = _userEvents

    private val _publicEvents = MutableStateFlow<Resource<List<Event>>>(Resource.Loading())
    val publicEvents: StateFlow<Resource<List<Event>>> = _publicEvents

    private val _userTickets = MutableStateFlow<Resource<List<Ticket>>>(Resource.Loading())
    val userTickets: StateFlow<Resource<List<Ticket>>> = _userTickets

    init {
        loadCurrentUser()
        fetchPublicEvents()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val userId = userRepository.getCurrentUserId()
            if (userId != null) {
                val user = userRepository.getUserByIdFromRemote(userId)
                _currentUser.value = user

                // Load user-specific data
                if (user != null) {
                    if (user.isOrganizer) {
                        fetchUserEvents(userId)
                    }
                    fetchUserTickets(userId)
                }
            }
        }
    }

    private fun fetchUserEvents(userId: String) {
        viewModelScope.launch {
            eventRepository.getEventsByOrganizerFromRemote(userId)
                .catch { e ->
                    _userEvents.value = Resource.Error(e.message ?: "Unknown error occurred")
                }
                .collectLatest { resource ->
                    _userEvents.value = resource
                }
        }
    }

    private fun fetchPublicEvents() {
        viewModelScope.launch {
            eventRepository.getPublicEventsFromRemote()
                .catch { e ->
                    _publicEvents.value = Resource.Error(e.message ?: "Unknown error occurred")
                }
                .collectLatest { resource ->
                    _publicEvents.value = resource
                }
        }
    }

    private fun fetchUserTickets(userId: String) {
        viewModelScope.launch {
            ticketRepository.getUserTicketsFromRemote(userId)
                .catch { e ->
                    _userTickets.value = Resource.Error(e.message ?: "Unknown error occurred")
                }
                .collectLatest { resource ->
                    _userTickets.value = resource
                }
        }
    }

    fun searchEvents(query: String) {
        // This would implement search functionality
        // For now, just reloading public events
        fetchPublicEvents()
    }

    fun refreshData() {
        val userId = userRepository.getCurrentUserId()
        if (userId != null) {
            val user = _currentUser.value
            if (user != null && user.isOrganizer) {
                fetchUserEvents(userId)
            }
            fetchUserTickets(userId)
        }
        fetchPublicEvents()
    }

    fun signOut() {
        userRepository.signOut()
    }

    fun isUserOrganizer(): Boolean {
        return _currentUser.value?.isOrganizer == true
    }
}