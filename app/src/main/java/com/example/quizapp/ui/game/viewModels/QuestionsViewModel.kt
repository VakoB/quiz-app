package com.example.quizapp.ui.game.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.quizapp.data.models.Question
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class QuestionCreationViewModel : ViewModel() {
    private val _questions = MutableLiveData<List<Question>>(emptyList())
    val questions: LiveData<List<Question>> = _questions

    fun addQuestion() {
        val currentList = _questions.value.orEmpty().toMutableList()
        currentList.add(Question(questionId = UUID.randomUUID().toString()))
        _questions.value = currentList
    }

    fun updateQuestion(questionId: String, newText: String, newOptions: List<String>, newCorrectIndex: Int) {
        val currentList = _questions.value.orEmpty().toMutableList()
        val index = currentList.indexOfFirst { it.questionId == questionId }
        if (index != -1) {
            currentList[index] = currentList[index].copy(
                questionText = newText,
                options = newOptions,
                correctIndex = newCorrectIndex
            )
            _questions.value = currentList
        }
    }

    fun publishQuestions(groupId: String, onComplete: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val batch = db.batch()

        _questions.value?.forEach { question ->
            val docRef = db.collection("groups")
                .document(groupId)
                .collection("questions")
                .document(question.questionId)
            batch.set(docRef, question)
        }

        batch.commit()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }
}
