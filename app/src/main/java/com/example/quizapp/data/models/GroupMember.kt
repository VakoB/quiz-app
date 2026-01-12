package com.example.quizapp.data.models

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class GroupMember(
    val uid: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val role: String = "member",
    val currentScore: Int = 0,
    @ServerTimestamp val joinedAt: Date? = null
)