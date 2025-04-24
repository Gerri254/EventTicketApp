package com.example.eventticketapp.data.remote

import com.example.eventticketapp.data.model.Resource
import com.example.eventticketapp.data.model.User
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthService @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestoreService: FirestoreService
) {
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    fun isUserAuthenticated(): Boolean = currentUser != null

    suspend fun signUp(email: String, password: String, name: String): Resource<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user

            if (firebaseUser != null) {
                // Update profile with display name
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                firebaseUser.updateProfile(profileUpdates).await()

                // Create user in Firestore
                val user = User(
                    id = firebaseUser.uid,
                    name = name,
                    email = email,
                    photoUrl = null,
                    isOrganizer = false
                )

                firestoreService.saveUser(user)
                Resource.Success(user)
            } else {
                Resource.Error("Failed to create user")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred during registration")
        }
    }

    suspend fun signIn(email: String, password: String): Resource<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user

            if (firebaseUser != null) {
                val user = firestoreService.getUserById(firebaseUser.uid)
                if (user != null) {
                    Resource.Success(user)
                } else {
                    Resource.Error("User data not found")
                }
            } else {
                Resource.Error("Authentication failed")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred during login")
        }
    }

    suspend fun signInWithGoogle(credential: AuthCredential): Resource<User> {
        return try {
            val result = auth.signInWithCredential(credential).await()
            val firebaseUser = result.user

            if (firebaseUser != null) {
                // Check if user exists in Firestore
                var user = firestoreService.getUserById(firebaseUser.uid)

                if (user == null) {
                    // Create new user if not exists
                    user = User(
                        id = firebaseUser.uid,
                        name = firebaseUser.displayName ?: "",
                        email = firebaseUser.email ?: "",
                        photoUrl = firebaseUser.photoUrl?.toString(),
                        isOrganizer = false
                    )
                    firestoreService.saveUser(user)
                }

                Resource.Success(user)
            } else {
                Resource.Error("Google authentication failed")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred during Google sign-in")
        }
    }

    suspend fun resetPassword(email: String): Resource<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to send password reset email")
        }
    }

    fun signOut() {
        auth.signOut()
    }
}