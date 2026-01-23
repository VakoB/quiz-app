package com.example.quizapp.ui.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentHomeBinding
import com.example.quizapp.ui.auth.AuthViewModel
import kotlin.getValue

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        _binding = FragmentHomeBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)

        viewModel.loadCurrentUser()

        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            user?.let {
                val imageUri = if (it.imageUrl?.isNotEmpty() == true) it.imageUrl else null
                Glide.with(homeProfile)
                    .load(imageUri)
                    .placeholder(R.drawable.ic_profile)
                    .circleCrop()
                    .into(homeProfile)
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}