package com.example.eventticketapp.ui.tickets.viewer

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventticketapp.data.model.Event
import com.example.eventticketapp.data.model.Resource
import com.example.eventticketapp.data.model.Ticket
import com.example.eventticketapp.data.repository.EventRepository
import com.example.eventticketapp.data.repository.TicketRepository
import com.example.eventticketapp.util.QRGeneratorUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TicketViewModel @Inject constructor(
    private val ticketRepository: TicketRepository,
    private val eventRepository: EventRepository,
    private val qrGeneratorUtil: QRGeneratorUtil
) : ViewModel() {

    private val _ticket = MutableStateFlow<Resource<Ticket>>(Resource.Loading())
    val ticket: StateFlow<Resource<Ticket>> = _ticket

    private val _event = MutableStateFlow<Resource<Event>>(Resource.Loading())
    val event: StateFlow<Resource<Event>> = _event

    private val _qrCodeBitmap = MutableStateFlow<Bitmap?>(null)
    val qrCodeBitmap: StateFlow<Bitmap?> = _qrCodeBitmap

    fun loadTicket(ticketId: String) {
        viewModelScope.launch {
            try {
                val ticketData = ticketRepository.getTicketByIdFromLocal(ticketId)
                    ?: ticketRepository.getTicketByQrCodeFromRemote(ticketId)

                if (ticketData != null) {
                    _ticket.value = Resource.Success(ticketData)
                    loadEvent(ticketData.eventId)
                    generateQrCode(ticketData)
                } else {
                    _ticket.value = Resource.Error("Ticket not found")
                }
            } catch (e: Exception) {
                _ticket.value = Resource.Error(e.message ?: "Failed to load ticket")
            }
        }
    }

    private fun loadEvent(eventId: String) {
        viewModelScope.launch {
            try {
                val eventData = eventRepository.getEventByIdFromRemote(eventId)

                if (eventData != null) {
                    _event.value = Resource.Success(eventData)
                } else {
                    _event.value = Resource.Error("Event not found")
                }
            } catch (e: Exception) {
                _event.value = Resource.Error(e.message ?: "Failed to load event")
            }
        }
    }

    private fun generateQrCode(ticket: Ticket) {
        viewModelScope.launch {
            val qrData = ticket.qrCodeData
            val bitmap = qrGeneratorUtil.generateQRCode(qrData, 512)
            _qrCodeBitmap.value = bitmap
        }
    }
}