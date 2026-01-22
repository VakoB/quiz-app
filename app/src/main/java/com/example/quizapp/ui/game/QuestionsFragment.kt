package com.example.quizapp.ui.game

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.R
import com.example.quizapp.data.models.Question
import com.example.quizapp.databinding.FragmentQuestionsBinding
import com.example.quizapp.ui.game.adapters.QuestionAdapter
import com.example.quizapp.ui.groups.GroupDetailsFragmentDirections
import java.util.UUID

class QuestionsFragment : Fragment() {

    private var _binding: FragmentQuestionsBinding ?= null
    private val binding get() = _binding!!

    private val viewModel: GameViewModel by viewModels()
    private lateinit var adapter: QuestionAdapter

    private val questions = mutableListOf<Question>()

    private var groupId: String = ""

    private val args: QuestionsFragmentArgs by navArgs()
    private var questionsList: MutableList<Question> = mutableListOf()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentQuestionsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)
        groupId = args.groupId

        /*setupRecyclerView()
        setupButtons()*/


        val questionAdapter = QuestionAdapter(questionsList)

        questionsRv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = questionAdapter
        }

        addQuestionFab.setOnClickListener {
            val sheet = InputQuestionsBottomSheet { question ->
                questionAdapter.addQuestion(question)
            }

            sheet.show(parentFragmentManager, "InputQuestionSheet")
        }

        startGameButton.setOnClickListener {

            val questions = questionAdapter.getQuestions()

            if (questions.isEmpty()) {
                Toast.makeText(requireContext(), "Add at least one question", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (questions.any { it.correctIndex == -1 }) {
                Toast.makeText(requireContext(), "Select correct answer for all questions", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.publishQuestionsAndStartWaitingGame(groupId, questions) { success ->
                if (success) {
                    findNavController().navigate(
                        QuestionsFragmentDirections
                            .actionQuestionsFragmentToGameLobbyFragment(groupId)
                    )
                } else {
                    Toast.makeText(requireContext(), "Failed to start game", Toast.LENGTH_SHORT).show()
                }
            }
        }


    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}