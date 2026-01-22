package com.example.quizapp.ui.game

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.R
import com.example.quizapp.data.models.MemberItem
import com.example.quizapp.databinding.FragmentLeaderBoardBinding
import com.example.quizapp.ui.groups.adapters.MembersAdapter
import com.example.quizapp.ui.groups.viewModels.GroupsViewModel


class LeaderBoardFragment : Fragment() {

    private var _binding: FragmentLeaderBoardBinding ?= null
    private val binding get() = _binding!!
    private val args: LeaderBoardFragmentArgs by navArgs()
    private val viewModel: GameViewModel by viewModels()
    private val groupsViewModel: GroupsViewModel by viewModels()
    private var groupId: String = ""
    private val membersAdapter = MembersAdapter()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLeaderBoardBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)
        groupId = args.groupId

        leaderboardRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = membersAdapter
        }

        viewModel.listenGamePlayers(groupId)

        viewModel.players.observe(viewLifecycleOwner) { players ->
            val leaderboard = players
                .sortedByDescending { it.currentScore }

            val uiItems = leaderboard.map { player ->
                MemberItem(
                    uid = player.uid,
                    firstName = player.firstName,
                    lastName = player.lastName,
                    score = player.currentScore,
                    role = player.role
                )
            }
            membersAdapter.submitList(uiItems)
        }

        finishGameAndGoBackButton.setOnClickListener {
            viewModel.finalizeGame(groupId)

            findNavController().navigate(
                LeaderBoardFragmentDirections.actionLeaderBoardFragmentToMainFragment()
            )
        }

        goBackButton.setOnClickListener {
            findNavController().navigate(
                LeaderBoardFragmentDirections.actionLeaderBoardFragmentToMainFragment()
            )
        }

        groupsViewModel.loadGroup(groupId)

        groupsViewModel.isOwner.observe(viewLifecycleOwner) { isOwner ->
            if (isOwner) {
                finishGameAndGoBackButton.visibility = View.VISIBLE
                goBackButton.visibility = View.GONE
            } else {
                finishGameAndGoBackButton.visibility = View.GONE
                goBackButton.visibility = View.VISIBLE
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}