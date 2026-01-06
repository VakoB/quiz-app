package com.example.quizapp.ui.game

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.quizapp.databinding.FragmentCreateGroupBinding

class CreateGroupFragment : Fragment() {
    private var _binding: FragmentCreateGroupBinding? = null
    private val binding get() = _binding!!
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentCreateGroupBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
