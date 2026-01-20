package com.example.eventticketapp.ui.tickets.preview

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
class TicketPreviewViewModel @Inject constructor(
    private val ticketRepository: TicketRepository,
    private val eventRepository: EventRepository,
    val qrGeneratorUtil: QRGeneratorUtil
) : ViewModel() {

    private val _ticket = MutableStateFlow<Resource<Ticket>>(Resource.Loading())
    val ticket: StateFlow<Resource<Ticket>> = _ticket

    private val _event = MutableStateFlow<Resource<Event>>(Resource.Loading())
    val event: StateFlow<Resource<Event>> = _event

    private val _qrCodeBitmap = MutableStateFlow<Bitmap?>(null)
    val qrCodeBitmap: StateFlow<Bitmap?> = _qrCodeBitmap

    fun loadTicket(ticketId: String) {
        viewModelScope.launch {
            _ticket.value = Resource.Loading()
            val ticketResult = ticketRepository.getTicketByIdFromRemote(ticketId)
            
            if (ticketResult != null) {
                _ticket.value = Resource.Success(ticketResult)
                loadEvent(ticketResult.eventId)
                generateQrCode(ticketResult)
            } else {
                _ticket.value = Resource.Error("Ticket not found")
            }
        }
    }

    private fun loadEvent(eventId: String) {
        viewModelScope.launch {
            _event.value = Resource.Loading()
            // Using repo
            val event = eventRepository.getEventByIdFromRemote(eventId) // or local
            if (event != null) {
                _event.value = Resource.Success(event)
            } else {
                _event.value = Resource.Error("Event not found")
            }
        }
    }

    private fun generateQrCode(ticket: Ticket) {
        viewModelScope.launch {
            try {
                val qrData = qrGeneratorUtil.generateTicketQRCodeData(
                    ticketId = ticket.id,
                    eventId = ticket.eventId,
                    userId = ticket.userId
                )
                val bitmap = qrGeneratorUtil.generateQRCode(qrData)
                _qrCodeBitmap.value = bitmap
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
