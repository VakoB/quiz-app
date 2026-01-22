package com.example.quizapp.data.models

data class MemberItem(
    val uid: String,
    val firstName: String,
    val lastName: String,
    val score: Int? = null,
    val isCorrect: Boolean? = null,
    val role: String = "member"
)