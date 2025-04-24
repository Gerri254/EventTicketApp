package com.example.eventticketapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.eventticketapp.data.local.entity.TicketEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TicketDao {
    @Query("SELECT * FROM tickets WHERE userId = :userId")
    fun getTicketsByUser(userId: String): Flow<List<TicketEntity>>

    @Query("SELECT * FROM tickets WHERE eventId = :eventId")
    fun getTicketsByEvent(eventId: String): Flow<List<TicketEntity>>

    @Query("SELECT * FROM tickets WHERE id = :ticketId")
    suspend fun getTicketById(ticketId: String): TicketEntity?

    @Query("SELECT * FROM tickets WHERE qrCodeData = :qrCodeData")
    suspend fun getTicketByQrCode(qrCodeData: String): TicketEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: TicketEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTickets(tickets: List<TicketEntity>)

    @Update
    suspend fun updateTicket(ticket: TicketEntity)

    @Delete
    suspend fun deleteTicket(ticket: TicketEntity)

    @Query("DELETE FROM tickets")
    suspend fun deleteAllTickets()

    @Query("SELECT COUNT(*) FROM tickets WHERE eventId = :eventId AND isScanned = 1")
    suspend fun getScannedTicketsCount(eventId: String): Int

    @Query("SELECT COUNT(*) FROM tickets WHERE eventId = :eventId")
    suspend fun getTotalTicketsCount(eventId: String): Int

    @Query("SELECT COUNT(*) FROM tickets WHERE ticketTypeId = :ticketTypeId")
    suspend fun getTicketsCountByType(ticketTypeId: String): Int
}