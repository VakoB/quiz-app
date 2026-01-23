package com.example.quizapp.ui.auth

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.quizapp.data.auth.AuthRepository
import com.example.quizapp.data.auth.UserRepository
import com.example.quizapp.data.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {
    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser
    private val _authState = MutableLiveData<AuthState>(AuthState.Idle)
    val authState: LiveData<AuthState> = _authState

    init {
        val user = authRepository.getCurrentUser()

        if (user != null) {
            userRepository.getUser(user.uid) { userDoc ->
                _authState.value = if (userDoc != null) {
                    AuthState.Authenticated
                } else {
                    AuthState.NeedsProfile
                }
            }
        } else {
            _authState.value = AuthState.Idle
        }
    }

    fun login(email: String, password: String) {
        _authState.value = AuthState.Loading
        authRepository.loginWithEmail(email, password) {
            _authState.value = it.fold(
                onSuccess = { AuthState.Success },
                onFailure = { e -> AuthState.Error(e.message ?: "Login failed") }
            )
        }
    }

    fun register(email: String, password: String) {
        _authState.value = AuthState.Loading
        authRepository.registerWithEmail(email, password) {
            _authState.value = it.fold(
                onSuccess = { AuthState.Success },
                onFailure = { e -> AuthState.Error(e.message ?: "Registration failed") }
            )
        }
    }

    fun loginWithGoogle(idToken: String) {
        _authState.value = AuthState.Loading
        authRepository.loginWithGoogle(idToken) { result ->
            result.fold(
                onSuccess = { firebaseUser ->
                    userRepository.userExists(firebaseUser.uid) { exists ->
                        _authState.value = if (exists) {
                            AuthState.Authenticated
                        } else {
                            AuthState.NeedsProfile
                        }
                    }
                },
                onFailure = { e -> AuthState.Error(e.message ?: "Google sign-in failed") }
            )
        }
    }

    private fun checkUserProfile(user: FirebaseUser) {
        userRepository.userExists(user.uid) { exists ->
            _authState.value = if (exists) AuthState.Authenticated
            else AuthState.NeedsProfile
        }
    }

    fun signOut() {
        authRepository.signOut()
        _authState.value = AuthState.Unauthenticated
    }

    fun loadCurrentUser() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            _currentUser.value = null
            return
        }

        userRepository.getUser(uid) { user ->
            if (user != null) {
                _currentUser.value = user
            } else {
                Log.d("AuthViewModel", "User not found in Firestore")
                _currentUser.value = null
            }
        }
    }

    fun completeProfile(firstName: String, lastName: String) {
        val firebaseUser = authRepository.getCurrentUser() ?: run {
            _authState.value = AuthState.Error("User not logged in")
            return
        }

        val user = User(
            uid = firebaseUser.uid,
            firstName = firstName,
            lastName = lastName,
            email = firebaseUser.email ?: "",
            imageUrl = firebaseUser.photoUrl?.toString() ?: "",
            provider = firebaseUser.providerData.firstOrNull()?.providerId ?: "google",
            completedProfile = true
        )

        userRepository.createUser(user) { success ->
            if (success) {
                _currentUser.value = user
                _authState.value = AuthState.Authenticated
            } else {
                _authState.value = AuthState.Error("Failed to save profile")
            }
        }
    }

}
