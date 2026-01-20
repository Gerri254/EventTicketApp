package com.example.eventticketapp.data.remote

import com.example.eventticketapp.data.model.Event
import com.example.eventticketapp.data.model.Resource
import com.example.eventticketapp.data.model.Ticket
import com.example.eventticketapp.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val USERS_COLLECTION = "users"
        private const val EVENTS_COLLECTION = "events"
        private const val TICKETS_COLLECTION = "tickets"
    }

    // User methods
    suspend fun saveUser(user: User) {
        firestore.collection(USERS_COLLECTION)
            .document(user.id)
            .set(user)
            .await()
    }

    suspend fun getUserById(userId: String): User? {
        val document = firestore.collection(USERS_COLLECTION)
            .document(userId)
            .get()
            .await()

        return if (document.exists()) {
            document.toObject(User::class.java)
        } else {
            null
        }
    }

    suspend fun updateUserRole(userId: String, isOrganizer: Boolean) {
        firestore.collection(USERS_COLLECTION)
            .document(userId)
            .update("isOrganizer", isOrganizer)
            .await()
    }

    // Event methods
    fun getAllEventsFlow(): Flow<Resource<List<Event>>> = callbackFlow {
        trySend(Resource.Loading())

        val subscription = firestore.collection(EVENTS_COLLECTION)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Unknown error"))
                    return@addSnapshotListener
                }

                val events = snapshot?.documents?.mapNotNull { it.toObject(Event::class.java) } ?: emptyList()
                trySend(Resource.Success(events))
            }

        awaitClose { subscription.remove() }
    }

    fun getPublicEventsFlow(): Flow<Resource<List<Event>>> = callbackFlow {
        trySend(Resource.Loading())

        val subscription = firestore.collection(EVENTS_COLLECTION)
            .whereEqualTo("isPublic", true)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Unknown error"))
                    return@addSnapshotListener
                }

                val events = snapshot?.documents?.mapNotNull { it.toObject(Event::class.java) } ?: emptyList()
                trySend(Resource.Success(events))
            }

        awaitClose { subscription.remove() }
    }

    fun getEventsByOrganizerFlow(organizerId: String): Flow<Resource<List<Event>>> = callbackFlow {
        trySend(Resource.Loading())

        val subscription = firestore.collection(EVENTS_COLLECTION)
            .whereEqualTo("organizerId", organizerId)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Unknown error"))
                    return@addSnapshotListener
                }

                val events = snapshot?.documents?.mapNotNull { it.toObject(Event::class.java) } ?: emptyList()
                trySend(Resource.Success(events))
            }

        awaitClose { subscription.remove() }
    }

    suspend fun getEventById(eventId: String): Event? {
        val document = firestore.collection(EVENTS_COLLECTION)
            .document(eventId)
            .get()
            .await()

        return if (document.exists()) {
            document.toObject(Event::class.java)
        } else {
            null
        }
    }

    suspend fun createEvent(event: Event): String {
        val eventRef = if (event.id.isEmpty()) {
            firestore.collection(EVENTS_COLLECTION).document()
        } else {
            firestore.collection(EVENTS_COLLECTION).document(event.id)
        }

        val eventId = eventRef.id
        val updatedEvent = event.copy(id = eventId)

        eventRef.set(updatedEvent).await()
        return eventId
    }

    suspend fun updateEvent(event: Event) {
        firestore.collection(EVENTS_COLLECTION)
            .document(event.id)
            .set(event)
            .await()
    }

    suspend fun deleteEvent(eventId: String) {
        firestore.collection(EVENTS_COLLECTION)
            .document(eventId)
            .delete()
            .await()
    }

    // Ticket methods
    suspend fun createTicket(ticket: Ticket): String {
        val ticketRef = if (ticket.id.isEmpty()) {
            firestore.collection(TICKETS_COLLECTION).document()
        } else {
            firestore.collection(TICKETS_COLLECTION).document(ticket.id)
        }

        val ticketId = ticketRef.id
        val updatedTicket = ticket.copy(id = ticketId)

        ticketRef.set(updatedTicket).await()
        return ticketId
    }

    suspend fun getTicketById(ticketId: String): Ticket? {
        val document = firestore.collection(TICKETS_COLLECTION)
            .document(ticketId)
            .get()
            .await()

        return if (document.exists()) {
            document.toObject(Ticket::class.java)
        } else {
            null
        }
    }

    suspend fun getTicketByQrCode(qrCodeData: String): Ticket? {
        val querySnapshot = firestore.collection(TICKETS_COLLECTION)
            .whereEqualTo("qrCodeData", qrCodeData)
            .get()
            .await()

        return querySnapshot.documents.firstOrNull()?.toObject(Ticket::class.java)
    }

    suspend fun updateTicketScanStatus(ticketId: String, isScanned: Boolean) {
        val updates = hashMapOf<String, Any?>(
            "isScanned" to isScanned,
            "scannedAt" to if (isScanned) Date() else null
        )

        firestore.collection(TICKETS_COLLECTION)
            .document(ticketId)
            .update(updates)
            .await()
    }

    fun getUserTicketsFlow(userId: String): Flow<Resource<List<Ticket>>> = callbackFlow {
        trySend(Resource.Loading())

        val subscription = firestore.collection(TICKETS_COLLECTION)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Unknown error"))
                    return@addSnapshotListener
                }

                val tickets = snapshot?.documents?.mapNotNull { it.toObject(Ticket::class.java) } ?: emptyList()
                trySend(Resource.Success(tickets))
            }

        awaitClose { subscription.remove() }
    }

    suspend fun getTicketsForEvent(eventId: String): List<Ticket> {
        val querySnapshot = firestore.collection(TICKETS_COLLECTION)
            .whereEqualTo("eventId", eventId)
            .get()
            .await()

        return querySnapshot.documents.mapNotNull { it.toObject(Ticket::class.java) }
    }
}