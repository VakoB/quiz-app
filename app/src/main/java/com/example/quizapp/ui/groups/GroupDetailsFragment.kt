package com.example.quizapp.ui.groups

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentGroupDetailsBinding
import com.example.quizapp.ui.groups.adapters.MembersAdapter
import com.example.quizapp.ui.groups.viewModels.GroupsViewModel

class GroupDetailsFragment : Fragment() {

    private var _binding: FragmentGroupDetailsBinding ?= null
    private val binding get() = _binding!!

    private val args: GroupDetailsFragmentArgs by navArgs()
    private val viewModel: GroupsViewModel by viewModels()

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
        groupDetailsGroupNameTv.text = args.groupId

        groupMembersRv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = membersAdapter
        }

        val groupId = args.groupId

        viewModel.group.observe(viewLifecycleOwner) { group ->
            groupDetailsGroupNameTv.text = group?.groupName
            groupDetailsCodeTv.text = group?.groupCode
        }

        viewModel.members.observe(viewLifecycleOwner) { members ->
            membersAdapter.submitList(members)
        }

        viewModel.loadGroup(groupId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}