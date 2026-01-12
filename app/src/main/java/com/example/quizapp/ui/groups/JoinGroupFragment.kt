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
import com.example.quizapp.databinding.FragmentJoinGroupBinding
import com.example.quizapp.ui.groups.viewModels.GroupsViewModel

class JoinGroupFragment : Fragment() {

    private var _binding: FragmentJoinGroupBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GroupsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentJoinGroupBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)

        joinGroupButton.setOnClickListener {
            val joinGroupCode = joinGroupEt.text.toString()
            viewModel.joinGroup(joinGroupCode)
        }

        viewModel.navigateToGroupDetails.observe(viewLifecycleOwner) { groupId ->
            if (groupId != null) {
                val action =
                    JoinGroupFragmentDirections.actionJoinGroupFragmentToGroupDetailsFragment(
                        groupId
                    )
                findNavController().navigate(
                    action,
                    NavOptions.Builder()
                        .setPopUpTo(
                            R.id.joinGroupFragment,
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