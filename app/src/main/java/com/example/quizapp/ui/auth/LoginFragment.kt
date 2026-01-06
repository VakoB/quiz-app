package com.example.quizapp.ui.auth

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.quizapp.R
import com.example.quizapp.databinding.AuthFragmentLoginBinding

import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.logger.Logger
import kotlinx.coroutines.launch


class LoginFragment : Fragment() {
    private var _binding: AuthFragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()
    private lateinit var credentialManager: CredentialManager



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AuthFragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        credentialManager = CredentialManager.create(requireContext())
        binding.apply {
            btnGoogleLogin.setOnClickListener {
                startGoogleSignIn()
            }
        }
    }

    private fun startGoogleSignIn() {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(getString(R.string.default_web_client_id))
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(false)
            .build()


        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(
                    context = requireActivity(),
                    request = request
                )

                handleSignIn(result.credential)

            } catch (e: GetCredentialException) {
                showError("Google sign-in cancelled")
                Log.e("GoogleSignIn", e.message ?: "No message", e)
            }
        }
    }

    private fun handleSignIn(credential: Credential) {
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleIdTokenCredential =
                GoogleIdTokenCredential.createFrom(credential.data)

            val idToken = googleIdTokenCredential.idToken
            viewModel.loginWithGoogle(idToken)
        }
    }

    private fun showError(errorMessage: String) {

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}