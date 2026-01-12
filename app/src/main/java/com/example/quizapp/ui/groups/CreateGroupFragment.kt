package com.example.quizapp.ui.groups

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentCreateGroupBinding
import com.example.quizapp.ui.groups.viewModels.GroupsViewModel


class CreateGroupFragment : Fragment() {

    private var _binding: FragmentCreateGroupBinding ?= null
    private val viewModel: GroupsViewModel by viewModels()
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreateGroupBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)

        createGroupButton.setOnClickListener {
            val groupName = createGroupGroupNameEt.text.toString()
            viewModel.createGroup(groupName)
        }

        viewModel.navigateToGroupDetails.observe(viewLifecycleOwner) { groupId ->
            if (groupId != null) {
                val action =
                    CreateGroupFragmentDirections
                        .actionCreateGroupFragmentToGroupDetailsFragment(groupId)

                findNavController().navigate(
                    action,
                    NavOptions.Builder()
                        .setPopUpTo(
                            R.id.createGroupFragment,
                            true
                        )
                        .build()
                )
                viewModel.onNavigationHandled()
            }
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}