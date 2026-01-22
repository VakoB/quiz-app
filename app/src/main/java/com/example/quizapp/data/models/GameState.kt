package com.example.quizapp.data.models

data class GameState(
    val status: String = "WAITING",
    val currentQuestionIndex: Int = 0,
    val phaseEndTime: Long = 0L
)

