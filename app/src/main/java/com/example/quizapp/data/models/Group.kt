package com.example.quizapp.data.models

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Group(
    val groupId: String = "",
    val groupName: String = "",
    val ownerId: String = "",
    val groupCode: String = "",
    @ServerTimestamp val createdAt: Date? = null
)
