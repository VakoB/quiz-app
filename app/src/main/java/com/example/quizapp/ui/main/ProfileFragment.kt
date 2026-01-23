package com.example.quizapp.ui.main

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentProfileBinding
import com.example.quizapp.ui.auth.AuthState
import com.example.quizapp.ui.auth.AuthViewModel


class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()
    private var firstTime = true


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)

        signOutButton.setOnClickListener {
            viewModel.signOut()
        }

        viewModel.authState.observe(viewLifecycleOwner) { state ->
            if (state == AuthState.Unauthenticated) {
                if (firstTime) {
                    firstTime = false
                    navigateToLogin()
                }
            }
        }

        viewModel.loadCurrentUser()

        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            user?.let {
                tvName.text = "${it.firstName} ${it.lastName}"
                val imageUri = if (it.imageUrl?.isNotEmpty() == true) it.imageUrl else null
                if (imageUri == null) {
                    Log.d("ProfileFragment", "Image URL is empty or null")
                }
                Glide.with(profileImageView)
                    .load(imageUri)
                    .placeholder(R.drawable.ic_profile)
                    .circleCrop()
                    .into(profileImageView)
            }
        }
    }


    private fun navigateToLogin() {
        val mainNavController = requireActivity().findNavController(R.id.nav_host)
        //val action = MainFragmentDirections.actionMainFragmentToGroupDetailsFragment(group.groupId)
        mainNavController.navigate(R.id.auth_nav_graph)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}