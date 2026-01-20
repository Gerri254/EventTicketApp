package com.example.eventticketapp.data.repository

import com.example.eventticketapp.data.local.dao.TicketDao
import com.example.eventticketapp.data.local.entity.TicketEntity
import com.example.eventticketapp.data.model.Resource
import com.example.eventticketapp.data.model.Ticket
import com.example.eventticketapp.data.remote.FirestoreService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TicketRepository @Inject constructor(
    private val ticketDao: TicketDao,
    private val firestoreService: FirestoreService
) {
    // Local operations
    fun getTicketsByUserFromLocal(userId: String): Flow<List<Ticket>> {
        return ticketDao.getTicketsByUser(userId).map { entities ->
            entities.map { it.toTicket() }
        }
    }

    fun getTicketsByEventFromLocal(eventId: String): Flow<List<Ticket>> {
        return ticketDao.getTicketsByEvent(eventId).map { entities ->
            entities.map { it.toTicket() }
        }
    }

    suspend fun getTicketByIdFromLocal(ticketId: String): Ticket? {
        return ticketDao.getTicketById(ticketId)?.toTicket()
    }

    suspend fun getTicketByQrCodeFromLocal(qrCodeData: String): Ticket? {
        return ticketDao.getTicketByQrCode(qrCodeData)?.toTicket()
    }

    suspend fun saveTicketToLocal(ticket: Ticket) {
        ticketDao.insertTicket(TicketEntity.fromTicket(ticket))
    }

    suspend fun saveTicketsToLocal(tickets: List<Ticket>) {
        ticketDao.insertTickets(tickets.map { TicketEntity.fromTicket(it) })
    }

    suspend fun updateTicketInLocal(ticket: Ticket) {
        ticketDao.updateTicket(TicketEntity.fromTicket(ticket))
    }

    suspend fun deleteTicketFromLocal(ticket: Ticket) {
        ticketDao.deleteTicket(TicketEntity.fromTicket(ticket))
    }

    suspend fun clearAllTicketsFromLocal() {
        ticketDao.deleteAllTickets()
    }

    suspend fun getScannedTicketsCountFromLocal(eventId: String): Int {
        return ticketDao.getScannedTicketsCount(eventId)
    }

    suspend fun getTotalTicketsCountFromLocal(eventId: String): Int {
        return ticketDao.getTotalTicketsCount(eventId)
    }

    suspend fun getTicketsCountByTypeFromLocal(ticketTypeId: String): Int {
        return ticketDao.getTicketsCountByType(ticketTypeId)
    }

    // Remote operations
    suspend fun createTicketInRemote(ticket: Ticket): String {
        return firestoreService.createTicket(ticket)
    }

    suspend fun getTicketByIdFromRemote(ticketId: String): Ticket? {
        return firestoreService.getTicketById(ticketId)
    }

    suspend fun getTicketByQrCodeFromRemote(qrCodeData: String): Ticket? {
        return firestoreService.getTicketByQrCode(qrCodeData)
    }

    suspend fun updateTicketScanStatusInRemote(ticketId: String, isScanned: Boolean) {
        firestoreService.updateTicketScanStatus(ticketId, isScanned)
    }

    fun getUserTicketsFromRemote(userId: String): Flow<Resource<List<Ticket>>> {
        return firestoreService.getUserTicketsFlow(userId)
    }

    suspend fun getTicketsForEventFromRemote(eventId: String): List<Ticket> {
        return firestoreService.getTicketsForEvent(eventId)
    }

    // Sync operations - these would handle the sync logic between local and remote
}