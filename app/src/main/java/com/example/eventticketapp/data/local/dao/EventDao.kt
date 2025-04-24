package com.example.eventticketapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.eventticketapp.data.local.entity.EventEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY date DESC")
    fun getAllEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE isPublic = 1 ORDER BY date DESC")
    fun getPublicEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE organizerId = :userId ORDER BY date DESC")
    fun getEventsByOrganizer(userId: String): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE date >= :currentDate ORDER BY date ASC")
    fun getUpcomingEvents(currentDate: Date): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :eventId")
    suspend fun getEventById(eventId: String): EventEntity?

    @Query("SELECT * FROM events WHERE category = :category ORDER BY date DESC")
    fun getEventsByCategory(category: String): Flow<List<EventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<EventEntity>)

    @Update
    suspend fun updateEvent(event: EventEntity)

    @Delete
    suspend fun deleteEvent(event: EventEntity)

    @Query("DELETE FROM events")
    suspend fun deleteAllEvents()

    @Query("SELECT * FROM events WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR location LIKE '%' || :query || '%'")
    fun searchEvents(query: String): Flow<List<EventEntity>>
}