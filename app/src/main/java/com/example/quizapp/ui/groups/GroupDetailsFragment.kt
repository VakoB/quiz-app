package com.example.quizapp.ui.groups

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
import com.example.quizapp.data.models.MemberItem
import com.example.quizapp.databinding.FragmentGroupDetailsBinding
import com.example.quizapp.ui.game.GameViewModel
import com.example.quizapp.ui.groups.adapters.MembersAdapter
import com.example.quizapp.ui.groups.viewModels.GroupsViewModel

class GroupDetailsFragment : Fragment() {

    private var _binding: FragmentGroupDetailsBinding ?= null
    private val binding get() = _binding!!

    private val args: GroupDetailsFragmentArgs by navArgs()
    private val groupsViewModel: GroupsViewModel by viewModels()
    private val gameViewModel: GameViewModel by viewModels()
    private val membersAdapter = MembersAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGroupDetailsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)
        val groupId = args.groupId

        groupDetailsCreateGameButton.setOnClickListener {
            val action = GroupDetailsFragmentDirections.actionGroupDetailsFragmentToQuestionsFragment(groupId)
            findNavController().navigate(action)
        }

        groupMembersRv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = membersAdapter
        }

        groupsViewModel.group.observe(viewLifecycleOwner) { group ->
            groupDetailsGroupNameTv.text = group?.groupName
            groupDetailsCodeTv.text = group?.groupCode
        }

        groupsViewModel.members.observe(viewLifecycleOwner) { members ->
            val uiItems = members.map { member ->
                MemberItem(
                    uid = member.uid,
                    firstName = member.firstName,
                    lastName = member.lastName,
                    score = member.currentScore,
                    role = member.role
                )
            }
            membersAdapter.submitList(uiItems)
        }

        groupsViewModel.loadGroup(groupId)
        groupsViewModel.listenGameExists(groupId)


        groupsViewModel.isOwner.observe(viewLifecycleOwner) { isOwner ->
            groupsViewModel.isGameActive.observe(viewLifecycleOwner) { gameActive ->
                when {
                    isOwner -> {
                        groupDetailsCreateGameButton.visibility = View.VISIBLE
                        groupDetailsJoinGameButton.visibility = View.GONE
                    }
                    !isOwner && gameActive -> {
                        groupDetailsCreateGameButton.visibility = View.GONE
                        groupDetailsJoinGameButton.visibility = View.VISIBLE
                    }
                    else -> {
                        groupDetailsCreateGameButton.visibility = View.GONE
                        groupDetailsJoinGameButton.visibility = View.GONE
                    }
                }
            }

        }

        groupDetailsJoinGameButton.setOnClickListener {
            gameViewModel.joinLobby(groupId) { success ->
                if (success) {
                    Toast.makeText(requireContext(), "Joined lobby!", Toast.LENGTH_SHORT).show()
                    val action = GroupDetailsFragmentDirections.actionGroupDetailsFragmentToGameLobbyFragment(groupId)
                    findNavController().navigate(action)
                } else {
                    Toast.makeText(requireContext(), "Failed to join", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}