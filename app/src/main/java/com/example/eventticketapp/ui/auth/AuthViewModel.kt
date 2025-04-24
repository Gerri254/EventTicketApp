package com.example.eventticketapp.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventticketapp.data.model.Resource
import com.example.eventticketapp.data.model.User
import com.example.eventticketapp.data.repository.UserRepository
import com.google.firebase.auth.AuthCredential
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<Resource<User>?>(null)
    val loginState: StateFlow<Resource<User>?> = _loginState

    private val _signupState = MutableStateFlow<Resource<User>?>(null)
    val signupState: StateFlow<Resource<User>?> = _signupState

    private val _passwordResetState = MutableStateFlow<Resource<Unit>?>(null)
    val passwordResetState: StateFlow<Resource<Unit>?> = _passwordResetState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = Resource.Loading()
            val result = userRepository.signIn(email, password)
            _loginState.value = result
        }
    }

    fun signUp(name: String, email: String, password: String) {
        viewModelScope.launch {
            _signupState.value = Resource.Loading()
            val result = userRepository.signUp(email, password, name)
            _signupState.value = result
        }
    }

    fun loginWithGoogle(credential: AuthCredential) {
        viewModelScope.launch {
            _loginState.value = Resource.Loading()
            val result = userRepository.signInWithGoogle(credential)
            _loginState.value = result
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _passwordResetState.value = Resource.Loading()
            val result = userRepository.resetPassword(email)
            _passwordResetState.value = result
        }
    }

    fun clearLoginState() {
        _loginState.value = null
    }

    fun clearSignupState() {
        _signupState.value = null
    }

    fun clearPasswordResetState() {
        _passwordResetState.value = null
    }
}
