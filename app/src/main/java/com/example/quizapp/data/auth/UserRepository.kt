package com.example.quizapp.data.auth

import com.example.quizapp.data.models.User
import com.google.firebase.firestore.FirebaseFirestore

class UserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val usersCollection = firestore.collection("users")

    fun userExists(uid: String, callback: (Boolean) -> Unit) {
        usersCollection.document(uid)
            .get()
            .addOnSuccessListener { document ->
                callback(document.exists())
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    fun createUser(user: User, onComplete: (Boolean) -> Unit) {
        usersCollection.document(user.uid)
            .set(user)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }


    fun getUser(uid: String, callback: (User?) -> Unit) {
        usersCollection.document(uid)
            .get()
            .addOnSuccessListener { doc ->
                callback(doc.toObject(User::class.java))
            }
            .addOnFailureListener { callback(null) }
    }
}