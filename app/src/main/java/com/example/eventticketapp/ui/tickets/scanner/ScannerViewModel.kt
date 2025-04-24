package com.example.eventticketapp.ui.tickets.scanner

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
class ScannerViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val ticketRepository: TicketRepository,
    private val qrGeneratorUtil: QRGeneratorUtil
) : ViewModel() {

    private val _scanState = MutableStateFlow<Resource<Ticket>?>(null)
    val scanState: StateFlow<Resource<Ticket>?> = _scanState

    private val _event = MutableStateFlow<Event?>(null)
    val event: StateFlow<Event?> = _event

    private val _scanStats = MutableStateFlow<ScanStats?>(null)
    val scanStats: StateFlow<ScanStats?> = _scanStats

    fun loadEvent(eventId: String) {
        viewModelScope.launch {
            val eventData = eventRepository.getEventByIdFromRemote(eventId)
            _event.value = eventData

            if (eventData != null) {
                updateScanStats(eventId)
            }
        }
    }

    fun processQrCode(qrCodeData: String) {
        viewModelScope.launch {
            _scanState.value = Resource.Loading()

            try {
                // Parse QR code data
                val ticketData = qrGeneratorUtil.parseQRCodeData(qrCodeData)

                if (ticketData != null) {
                    val ticketId = ticketData["ticketId"]
                    val eventId = ticketData["eventId"]

                    if (ticketId != null && eventId != null) {
                        // Check if ticket exists
                        val ticket = ticketRepository.getTicketByQrCodeFromRemote(qrCodeData)

                        if (ticket != null) {
                            if (ticket.isScanned) {
                                _scanState.value = Resource.Error("Ticket already scanned")
                            } else {
                                // Mark ticket as scanned
                                ticketRepository.updateTicketScanStatusInRemote(ticket.id, true)

                                // Get updated ticket
                                val updatedTicket = ticket.copy(isScanned = true)
                                _scanState.value = Resource.Success(updatedTicket)

                                // Update scan stats
                                updateScanStats(ticket.eventId)
                            }
                        } else {
                            _scanState.value = Resource.Error("Invalid ticket")
                        }
                    } else {
                        _scanState.value = Resource.Error("Invalid QR code data")
                    }
                } else {
                    _scanState.value = Resource.Error("Could not parse QR code data")
                }
            } catch (e: Exception) {
                _scanState.value = Resource.Error("Error processing QR code: ${e.message}")
            }
        }
    }

    private suspend fun updateScanStats(eventId: String) {
        try {
            val tickets = ticketRepository.getTicketsForEventFromRemote(eventId)
            val scannedCount = tickets.count { it.isScanned }
            val totalCount = tickets.size

            _scanStats.value = ScanStats(
                scannedCount = scannedCount,
                totalCount = totalCount
            )
        } catch (e: Exception) {
            // Handle error
        }
    }

    fun clearScanState() {
        _scanState.value = null
    }

    data class ScanStats(
        val scannedCount: Int,
        val totalCount: Int
    )
}