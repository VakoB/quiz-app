package com.example.quizapp.data.models

data class GamePlayer(
    val firstName: String = "",
    val lastName: String = "",
    val role: String = "",
    val uid: String = "",
    val currentScore: Int = 0,
    val selectedOption: Int? = null,
    val isCorrect: Boolean? = null
)
