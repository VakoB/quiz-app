package com.example.quizapp.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    fun loginWithEmail(
        email: String,
        password: String,
        callback: (Result<FirebaseUser>) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { callback(Result.success(it.user!!)) }
            .addOnFailureListener { callback(Result.failure(it)) }
    }

    fun registerWithEmail(
        email: String,
        password: String,
        callback: (Result<FirebaseUser>) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { callback(Result.success(it.user!!)) }
            .addOnFailureListener { callback(Result.failure(it)) }
    }

    fun loginWithGoogle(
        idToken: String,
        callback: (Result<FirebaseUser>) -> Unit
    ) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnSuccessListener { callback(Result.success(it.user!!)) }
            .addOnFailureListener { callback(Result.failure(it)) }
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun signOut() {
        auth.signOut()
    }
}