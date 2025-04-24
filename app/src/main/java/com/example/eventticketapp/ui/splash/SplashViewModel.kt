package com.example.eventticketapp.ui.splash

import androidx.lifecycle.ViewModel
import com.example.eventticketapp.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    fun isUserLoggedIn(): Boolean {
        return userRepository.isUserAuthenticated()
    }
}