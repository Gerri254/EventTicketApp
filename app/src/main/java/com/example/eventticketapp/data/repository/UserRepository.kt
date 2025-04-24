package com.example.eventticketapp.data.repository

import com.example.eventticketapp.data.local.dao.UserDao
import com.example.eventticketapp.data.local.entity.UserEntity
import com.example.eventticketapp.data.model.Resource
import com.example.eventticketapp.data.model.User
import com.example.eventticketapp.data.remote.FirebaseAuthService
import com.example.eventticketapp.data.remote.FirestoreService
import com.google.firebase.auth.AuthCredential
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val firebaseAuthService: FirebaseAuthService,
    private val firestoreService: FirestoreService
) {
    // Local operations
    fun getAllUsersFromLocal(): Flow<List<User>> {
        return userDao.getAllUsers().map { entities ->
            entities.map { it.toUser() }
        }
    }

    suspend fun getUserByIdFromLocal(userId: String): User? {
        return userDao.getUserById(userId)?.toUser()
    }

    suspend fun getUserByEmailFromLocal(email: String): User? {
        return userDao.getUserByEmail(email)?.toUser()
    }

    suspend fun saveUserToLocal(user: User) {
        userDao.insertUser(UserEntity.fromUser(user))
    }

    suspend fun updateUserInLocal(user: User) {
        userDao.updateUser(UserEntity.fromUser(user))
    }

    suspend fun deleteUserFromLocal(user: User) {
        userDao.deleteUser(UserEntity.fromUser(user))
    }

    suspend fun clearAllUsersFromLocal() {
        userDao.deleteAllUsers()
    }

    // Remote operations
    suspend fun signUp(email: String, password: String, name: String): Resource<User> {
        val result = firebaseAuthService.signUp(email, password, name)
        if (result is Resource.Success && result.data != null) {
            saveUserToLocal(result.data)
        }
        return result
    }

    suspend fun signIn(email: String, password: String): Resource<User> {
        val result = firebaseAuthService.signIn(email, password)
        if (result is Resource.Success && result.data != null) {
            saveUserToLocal(result.data)
        }
        return result
    }

    suspend fun signInWithGoogle(credential: AuthCredential): Resource<User> {
        val result = firebaseAuthService.signInWithGoogle(credential)
        if (result is Resource.Success && result.data != null) {
            saveUserToLocal(result.data)
        }
        return result
    }

    suspend fun resetPassword(email: String): Resource<Unit> {
        return firebaseAuthService.resetPassword(email)
    }

    fun signOut() {
        firebaseAuthService.signOut()
    }

    suspend fun getUserFromRemote(userId: String): User? {
        return firestoreService.getUserById(userId)
    }

    suspend fun saveUserToRemote(user: User) {
        firestoreService.saveUser(user)
    }

    suspend fun updateUserRoleInRemote(userId: String, isOrganizer: Boolean) {
        firestoreService.updateUserRole(userId, isOrganizer)
    }

    fun isUserAuthenticated(): Boolean {
        return firebaseAuthService.isUserAuthenticated()
    }

    fun getCurrentUserId(): String? {
        return firebaseAuthService.currentUser?.uid
    }
}