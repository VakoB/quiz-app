package com.example.quizapp.ui.game

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.quizapp.R
import com.example.quizapp.data.models.Question
import com.example.quizapp.databinding.FragmentGameBinding
import com.example.quizapp.ui.groups.viewModels.GroupsViewModel


class GameFragment : Fragment() {

    private var _binding: FragmentGameBinding ?= null
    private val binding get() = _binding!!
    private val viewModel: GameViewModel by viewModels()
    private val groupsViewModel: GroupsViewModel by viewModels()
    private var groupId: String = ""
    private val args: GameFragmentArgs by navArgs()

    private lateinit var optionViews: List<TextView>
    private var selectedOptionIndex: Int? = null

    private var hasAnswered = false
    private var lastQuestionIndex: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentGameBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)
        groupId = args.groupId


        viewModel.loadQuestions(groupId)
        viewModel.listenGame(groupId)

        viewModel.currentQuestion.observe(viewLifecycleOwner) { question ->
            //if (isGameFinished) return@observe
            showQuestion(question)
        }

        viewModel.gameState.observe(viewLifecycleOwner) { state ->
            val newIndex = state.currentQuestionIndex

            if (newIndex != lastQuestionIndex) {
                lastQuestionIndex = newIndex
                resetOptions()
            }

            if (state.status == "FINISHED") {
                showGameFinished()
            }
            updateOwnerControls()
        }

        gameNextQuestion.setOnClickListener {
            cleanOptions()
            viewModel.nextQuestion(groupId)
        }

        gameFinishGame.setOnClickListener {
           // isGameFinished = true
            viewModel.finishGame(groupId)
        }


        optionViews = listOf(
            gameOption1,
            gameOption2,
            gameOption3,
            gameOption4
        )

        optionViews.forEachIndexed { index, textView ->
            textView.setOnClickListener {
                selectOption(index)
            }
        }


        groupsViewModel.loadGroup(groupId)

        groupsViewModel.isOwner.observe(viewLifecycleOwner) { isOwner ->
            if (isOwner) {
                gameNextQuestion.visibility = View.VISIBLE
            } else {
                gameNextQuestion.visibility = View.GONE
            }
        }
    }

    private fun showGameFinished() {
        Toast.makeText(requireContext(), "Game finished!", Toast.LENGTH_SHORT).show()
        val action = GameFragmentDirections.actionGameFragmentToLeaderBoardFragment(groupId)
        findNavController().navigate(action)
    }

    private fun showQuestion(question: Question) = with(binding) {
        gameQuestion.text = question.questionText
        gameOption1.text = question.options[0]
        gameOption2.text = question.options[1]
        gameOption3.text = question.options[2]
        gameOption4.text = question.options[3]
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun selectOption(selectedIndex: Int) {
        if (hasAnswered) return
        hasAnswered = true

        selectedOptionIndex = selectedIndex
        optionViews.forEachIndexed { index, textView ->
            textView.isSelected = index == selectedIndex
        }
        viewModel.submitAnswer(groupId, selectedIndex)
    }


    private fun cleanOptions() = with(binding) {
        optionViews.forEach { textView ->
            textView.isSelected = false
        }
    }

    private fun resetOptions() {
        hasAnswered = false
        selectedOptionIndex = null

        optionViews.forEach {
            it.isSelected = false
            it.isEnabled = true
        }
    }

    private fun updateOwnerControls() = with(binding) {
        val isOwner = groupsViewModel.isOwner.value ?: false
        val currentIndex = viewModel.gameState.value?.currentQuestionIndex ?: return
        val totalQuestions = viewModel.questions.value?.size ?: return

        if (!isOwner) {
            gameNextQuestion.visibility = View.GONE
            gameFinishGame.visibility = View.GONE
            return
        }

        val isLastQuestion = currentIndex >= totalQuestions - 1

        gameNextQuestion.visibility =
            if (isLastQuestion) View.GONE else View.VISIBLE

        gameFinishGame.visibility =
            if (isLastQuestion) View.VISIBLE else View.GONE
    }


}