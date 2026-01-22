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
import com.example.quizapp.data.models.MemberItem
import com.example.quizapp.databinding.FragmentGameLobbyBinding
import com.example.quizapp.ui.groups.adapters.MembersAdapter
import com.example.quizapp.ui.groups.viewModels.GroupsViewModel
import kotlin.getValue



class GameLobbyFragment : Fragment() {
    private var _binding: FragmentGameLobbyBinding? = null
    private val binding get() = _binding!!

    private val args: QuestionsFragmentArgs by navArgs()
    private var groupId: String = ""
    private val gameViewModel: GameViewModel by viewModels()
    private val membersAdapter = MembersAdapter()
    private val groupsViewModel: GroupsViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGameLobbyBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)
        groupId = args.groupId

        gameLobbyRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = membersAdapter
        }

        gameViewModel.listenGamePlayers(groupId)

        gameViewModel.players.observe(viewLifecycleOwner) { players ->
            val uiItems = players.map { player ->

                MemberItem(
                    uid = player.uid,
                    firstName = player.firstName,
                    lastName = player.lastName,
                    score = player.currentScore,
                    role = player.role,
                    isCorrect = player.isCorrect
                )
            }
            membersAdapter.submitList(uiItems)
        }

        gameViewModel.listenGame(groupId)

        gameViewModel.gameState.observe(viewLifecycleOwner) { state ->
            if (state.status == "QUESTION") {
                moveToGameFragment()
            }
        }

        lobbuStartGameButton.setOnClickListener {
            gameViewModel.startGame(groupId) { success ->
                if (success) {
                    moveToGameFragment()
                } else {
                    Toast.makeText(requireContext(), "Failed to start game", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

        groupsViewModel.loadGroup(groupId)
        groupsViewModel.isOwner.observe(viewLifecycleOwner) { isOwner ->
            if (isOwner) {
                lobbuStartGameButton.visibility = View.VISIBLE
            } else {
                lobbuStartGameButton.visibility = View.GONE
            }

        }
    }

    private fun moveToGameFragment() {
        val navController = findNavController()

        if (navController.currentDestination?.id == R.id.gameLobbyFragment) {
            val action =
                GameLobbyFragmentDirections.actionGameLobbyFragmentToGameFragment(groupId)
            navController.navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}