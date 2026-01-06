package com.example.quizapp.ui.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.quizapp.R
import com.example.quizapp.databinding.AuthFragmentRegisterBinding

class RegisterFragment : Fragment() {
    private var _binding: AuthFragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = AuthFragmentRegisterBinding.bind(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

    }

}