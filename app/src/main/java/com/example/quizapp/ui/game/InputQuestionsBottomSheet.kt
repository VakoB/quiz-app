package com.example.quizapp.ui.game

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.quizapp.R
import com.example.quizapp.data.models.Question
import com.example.quizapp.databinding.FragmentInputQuestionsBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.UUID

class InputQuestionsBottomSheet(
    private val onQuestionCreated: (Question) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: FragmentInputQuestionsBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInputQuestionsBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {

        bottomSheetSubmitButton.setOnClickListener {
            val questionText = bottomSheetQuestionEt.text.toString().trim()

            val options = listOf(
                bottomSheetOption1.text.toString().trim(),
                bottomSheetOption2.text.toString().trim(),
                bottomSheetOption3.text.toString().trim(),
                bottomSheetOption4.text.toString().trim()
            )

            if (questionText.isEmpty() || options.any { it.isEmpty() }) {
                Toast.makeText(requireContext(), "Fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val question = Question(
                questionId = UUID.randomUUID().toString(),
                questionText = questionText,
                options = options,
                correctIndex = -1
            )

            onQuestionCreated(question)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

