package com.example.quizapp.data.models

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class User(
    val uid: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val imageUrl: String? = null,
    val provider: String = "google",
    val completedProfile: Boolean = false,
    val joinedGroups: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Date? = null
)
