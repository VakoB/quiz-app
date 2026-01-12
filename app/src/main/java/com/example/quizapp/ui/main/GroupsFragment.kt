package com.example.quizapp.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.R
import com.example.quizapp.data.models.Group
import com.example.quizapp.databinding.FragmentGroupsBinding
import com.example.quizapp.ui.groups.AddGroupBottomSheet
import com.example.quizapp.ui.groups.GroupListItem
import com.example.quizapp.ui.groups.GroupsMainAdapter
import com.example.quizapp.ui.groups.JoinGroupFragmentDirections
import com.example.quizapp.ui.groups.viewModels.GroupsViewModel

class GroupsFragment : Fragment() {

    private var _binding: FragmentGroupsBinding ?= null
    private val binding get() = _binding!!

    private val mainAdapter by lazy {
        GroupsMainAdapter { selectedGroup ->
            handleGroupClick(selectedGroup)
        }
    }
    private val viewModel: GroupsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGroupsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)

        rvGroupsMain.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mainAdapter
        }

        viewModel.items.observe(viewLifecycleOwner) { items ->
            mainAdapter.submitList(items)
        }

        viewModel.loadGroups()

        fabAddGroup.setOnClickListener {
            AddGroupBottomSheet().show(parentFragmentManager, "AddGroupBottomSheet")
        }
    }

    private fun handleGroupClick(group: Group) {
        Toast.makeText(requireContext(), "${group.groupName} - ${group.ownerId}", Toast.LENGTH_SHORT).show()
        val mainNavController = requireActivity().findNavController(R.id.nav_host)
        val action = MainFragmentDirections.actionMainFragmentToGroupDetailsFragment(group.groupId)

        mainNavController.navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}