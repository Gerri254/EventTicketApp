package com.example.eventticketapp.data.repository

import android.net.Uri
import com.example.eventticketapp.data.local.dao.EventDao
import com.example.eventticketapp.data.local.entity.EventEntity
import com.example.eventticketapp.data.model.Event
import com.example.eventticketapp.data.model.Resource
import com.example.eventticketapp.data.remote.FirestoreService
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepository @Inject constructor(
    private val eventDao: EventDao,
    private val firestoreService: FirestoreService,
    private val storage: FirebaseStorage
) {
    suspend fun uploadImage(uri: Uri): String {
        val storageRef = storage.reference
        val imageRef = storageRef.child("events/${UUID.randomUUID()}/cover.jpg")
        val uploadTask = imageRef.putFile(uri).await()
        return imageRef.downloadUrl.await().toString()
    }

    // Local operations
    fun getAllEventsFromLocal(): Flow<List<Event>> {
        return eventDao.getAllEvents().map { entities ->
            entities.map { it.toEvent() }
        }
    }

    fun getPublicEventsFromLocal(): Flow<List<Event>> {
        return eventDao.getPublicEvents().map { entities ->
            entities.map { it.toEvent() }
        }
    }

    fun getEventsByOrganizerFromLocal(userId: String): Flow<List<Event>> {
        return eventDao.getEventsByOrganizer(userId).map { entities ->
            entities.map { it.toEvent() }
        }
    }

    fun getUpcomingEventsFromLocal(): Flow<List<Event>> {
        return eventDao.getUpcomingEvents(Date()).map { entities ->
            entities.map { it.toEvent() }
        }
    }

    fun searchEventsFromLocal(query: String): Flow<List<Event>> {
        return eventDao.searchEvents(query).map { entities ->
            entities.map { it.toEvent() }
        }
    }

    suspend fun saveEventImageUrl(imageUrl: String): Resource<String> {
        return try {
            // Just return the URL directly since we're not uploading
            Resource.Success(imageUrl)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to save image URL")
        }
    }




    suspend fun getEventByIdFromLocal(eventId: String): Event? {
        return eventDao.getEventById(eventId)?.toEvent()
    }

    suspend fun saveEventToLocal(event: Event) {
        eventDao.insertEvent(EventEntity.fromEvent(event))
    }

    suspend fun saveEventsToLocal(events: List<Event>) {
        eventDao.insertEvents(events.map { EventEntity.fromEvent(it) })
    }

    suspend fun updateEventInLocal(event: Event) {
        eventDao.updateEvent(EventEntity.fromEvent(event))
    }

    suspend fun deleteEventFromLocal(event: Event) {
        eventDao.deleteEvent(EventEntity.fromEvent(event))
    }

    suspend fun clearAllEventsFromLocal() {
        eventDao.deleteAllEvents()
    }

    // Remote operations
    fun getAllEventsFromRemote(): Flow<Resource<List<Event>>> {
        return firestoreService.getAllEventsFlow()
    }

    fun getPublicEventsFromRemote(): Flow<Resource<List<Event>>> {
        return firestoreService.getPublicEventsFlow()
    }

    fun getEventsByOrganizerFromRemote(organizerId: String): Flow<Resource<List<Event>>> {
        return firestoreService.getEventsByOrganizerFlow(organizerId)
    }

    suspend fun getEventByIdFromRemote(eventId: String): Event? {
        return firestoreService.getEventById(eventId)
    }

    suspend fun createEventInRemote(event: Event): String {
        return firestoreService.createEvent(event)
    }

    suspend fun updateEventInRemote(event: Event) {
        firestoreService.updateEvent(event)
    }

    suspend fun deleteEventFromRemote(eventId: String) {
        firestoreService.deleteEvent(eventId)
    }

    // Sync operations
    fun syncEvents(userId: String): Flow<Resource<List<Event>>> = flow {
        emit(Resource.Loading())
        try {
            // Fetch from remote
            val remoteEventsResource = firestoreService.getPublicEventsFlow()
            val organizerEventsResource = firestoreService.getEventsByOrganizerFlow(userId)

            // Save to local
            // This part would need to be implemented based on how to collect from the Flow

            emit(Resource.Success(emptyList())) // Placeholder
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to sync events"))
        }
    }
}