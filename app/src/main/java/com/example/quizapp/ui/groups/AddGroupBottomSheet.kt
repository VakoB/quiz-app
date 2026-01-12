package com.example.quizapp.ui.groups

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.quizapp.R
import com.example.quizapp.databinding.BottomSheetAddGroupBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddGroupBottomSheet : BottomSheetDialogFragment() {
    private var _binding: BottomSheetAddGroupBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAddGroupBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)
        val mainNavController = requireActivity().findNavController(R.id.nav_host)

       createGroupBottomSheetButton.setOnClickListener {
           mainNavController.navigate(R.id.action_mainFragment_to_createGroupFragment)
           dismiss()
        }

       joinGroupBottomSheetButton.setOnClickListener {
           mainNavController.navigate(R.id.action_mainFragment_to_joinGroupFragment)
           dismiss()
       }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
