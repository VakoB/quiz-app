package com.example.quizapp.ui.auth

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentCompleteProfileBinding

class CompleteProfileFragment : Fragment() {
    private var _binding: FragmentCompleteProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCompleteProfileBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loadCurrentUser()

        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            user?.let {
                firstNameTv.setText(it.firstName.ifEmpty { "" })
                lastNameTv.setText(it.lastName.ifEmpty { "" })

                val imageUri = if (it.imageUrl?.isNotEmpty() == true) it.imageUrl else null
                Glide.with(profileImageView)
                    .load(imageUri)
                    .placeholder(R.drawable.ic_profile)
                    .circleCrop()
                    .into(profileImageView)
            }
        }


        completeProfileButton.setOnClickListener {
            val firstName = firstNameTv.text.toString().trim()
            val lastName = lastNameTv.text.toString().trim()

            viewModel.completeProfile(firstName, lastName)
        }

        viewModel.authState.observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                AuthState.Authenticated -> navigateToMain()
                is AuthState.Error -> showError(state.message)
                else -> {}
            }
        })

    }

    private fun navigateToMain() {
        val mainNavController = requireActivity().findNavController(R.id.nav_host)
        //findNavController().navigate(R.id.main_nav_graph) {
         //   popUpTo(R.id.auth_nav_graph) {
        //        inclusive = true
        //    }
       // }
        mainNavController.navigate(R.id.mainFragment)
    }

    private fun showError(message: String) {
        // TODO: Show toast/snackbar
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}