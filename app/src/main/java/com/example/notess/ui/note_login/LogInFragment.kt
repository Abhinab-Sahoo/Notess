package com.example.notess.ui.note_login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.notess.R
import com.example.notess.databinding.FragmentLogInBinding
import com.example.notess.viewmodel.AuthViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LogInFragment : Fragment() {

    private var _binding: FragmentLogInBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var firebaseAuth: FirebaseAuth
    @Inject
    lateinit var credentialManager: CredentialManager
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentLogInBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeAuthState()

//        if (firebaseAuth.currentUser != null) {
//            navigateHome()
//            return
//        }
//
//        binding.googleSignInButton.setOnClickListener {
//            launchGoogleSignIn()
//        }

    }

    private fun observeAuthState() {
        authViewModel.syncStatus.observe(viewLifecycleOwner) { statusMessage ->
            showToast(statusMessage)
        }

        authViewModel.isSyncing.observe(viewLifecycleOwner) { isSyncing ->
            if (isSyncing) {
                binding.googleSignInButton.isEnabled = false
            } else {
                binding.googleSignInButton.isEnabled = true
            }
        }

        authViewModel.isUserLoggedIn.observe(viewLifecycleOwner) { isLoggedIn ->
            if (isLoggedIn) {
                lifecycleScope.launch {
                    delay(2000)
                    if (isAdded) {
                        navigateHome()
                    }
                }
            }
        }
    }

    private fun launchGoogleSignIn() {
        lifecycleScope.launch {

            try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(getString(R.string.web_client_id))
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(
                    context = requireContext(),
                    request = request
                )

                val credential = result.credential
                if (credential is CredentialManager &&
                    credential.type == GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {

                    val googleIdTokenCredential = GoogleIdTokenCredential.Companion.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken

                    if (idToken != null) {
                        signInWithFirebase(idToken)
                    } else {
                        authViewModel.onGoogleSignInFails("Failed to get ID token")
                    }
                } else {
                    authViewModel.onGoogleSignInFails("Invalid credential type: ${credential.type}")
                }

            } catch (e: Exception) {
                e.printStackTrace()
                authViewModel.onGoogleSignInFails("Sign in Failed: ${e.message}")
            }
        }
    }

    private fun signInWithFirebase(idToken: String) {

        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)

        firebaseAuth.signInWithCredential(firebaseCredential)
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    authViewModel.onGoogleSignInSuccess()
                } else {
                    authViewModel.onGoogleSignInFails("Firebase login failed: ${task.exception?.message}")
                }
            }
    }

    private fun showToast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    private fun navigateHome() {
        findNavController().navigate(R.id.action_logInFragment_to_noteFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}