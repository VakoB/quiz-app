package com.example.quizapp.ui.game.adapters

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.quizapp.data.models.Question
import com.example.quizapp.databinding.ItemQuestionBinding

class QuestionAdapter(
    private val questions: MutableList<Question>
) : RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder>() {


    inner class QuestionViewHolder(
        val binding: ItemQuestionBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val binding = ItemQuestionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return QuestionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        val question = questions[position]
        val b = holder.binding

        b.question.text = question.questionText

        val optionViews = listOf(
            b.option1,
            b.option2,
            b.option3,
            b.option4
        )

        optionViews.forEachIndexed { index, textView ->
            textView.text = question.options[index]

            // restore selection state
            textView.isSelected = question.correctIndex == index

            textView.setOnClickListener {
                // owner selects correct answer
                val updatedQuestion = question.copy(correctIndex = index)
                questions[position] = updatedQuestion
                notifyItemChanged(position)
            }
        }
    }


    override fun getItemCount(): Int = questions.size

    fun addQuestion(question: Question) {
        questions.add(question)
        notifyItemInserted(questions.size - 1)
    }

    fun getQuestions(): List<Question> = questions
}
