package com.example.quizapp.data.models

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Question(
    val questionId: String = "",
    val questionText: String = "",
    val options: List<String> = listOf("", "", "", ""),
    val correctIndex: Int = -1,
    val points: Int = 10,
    @ServerTimestamp val createdAt: Date? = null
)

