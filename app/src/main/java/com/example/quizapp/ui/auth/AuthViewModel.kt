package com.example.quizapp.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.quizapp.data.auth.AuthRepository

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _authState = MutableLiveData<AuthState>(AuthState.Idle)
    val authState: LiveData<AuthState> = _authState

    fun login(email: String, password: String) {
        _authState.value = AuthState.Loading
        repository.loginWithEmail(email, password) {
            _authState.value = it.fold(
                onSuccess = { AuthState.Success },
                onFailure = { e -> AuthState.Error(e.message ?: "Login failed") }
            )
        }
    }

    fun register(email: String, password: String) {
        _authState.value = AuthState.Loading
        repository.registerWithEmail(email, password) {
            _authState.value = it.fold(
                onSuccess = { AuthState.Success },
                onFailure = { e -> AuthState.Error(e.message ?: "Registration failed") }
            )
        }
    }

    fun loginWithGoogle(idToken: String) {
        _authState.value = AuthState.Loading
        repository.loginWithGoogle(idToken) {
            _authState.value = it.fold(
                onSuccess = { AuthState.Success },
                onFailure = { e -> AuthState.Error(e.message ?: "Google sign-in failed") }
            )
        }
    }
}
