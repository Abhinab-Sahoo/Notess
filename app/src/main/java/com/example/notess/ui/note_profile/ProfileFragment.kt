package com.example.notess.ui.note_profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.notess.R
import com.example.notess.databinding.FragmentProfileBinding
import com.example.notess.viewmodel.AuthViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
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
        _binding = FragmentProfileBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up the sign in/out button with proper logic
        setupSignInOutButton()

        // Update UI based on current auth state
        updateSignInOutUI()
    }

    private fun setupSignInOutButton() {
        binding.signInAndOutCardView.setOnClickListener {
            val currentUser = firebaseAuth.currentUser

            if (currentUser != null) {
                // User is logged in - sign them out
                signOut()
            } else {
                // User is not logged in - sign them in
                launchGoogleSignIn()
            }
        }
    }

    private fun updateSignInOutUI() {

        authViewModel.isUserLoggedIn.observe(viewLifecycleOwner) { isLoggedIn ->
            if (isLoggedIn) {
                // User is logged in - show Sign Out
                binding.signInAndOutTextView.text = "Sign Out"
                binding.SignInAndOutButton.setImageResource(R.drawable.logout)
                binding.displayName.text = firebaseAuth.currentUser?.displayName
                binding.email.text = firebaseAuth.currentUser?.email
            } else {
                // User is not logged in - show Sign In
                binding.signInAndOutTextView.text = "Sign In"
                binding.SignInAndOutButton.setImageResource(R.drawable.login)
                binding.deleteAccountCardView.visibility = View.GONE
            }
        }
    }

    private fun signOut() {
        firebaseAuth.signOut()

        // Clear any cached Google credentials
        lifecycleScope.launch {
            try {
                credentialManager.clearCredentialState(
                    ClearCredentialStateRequest()
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Update UI after sign out
            updateSignInOutUI()
            Toast.makeText(requireContext(), "Signed out successfully", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.noteFragment)
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
                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {

                    val googleIdTokenCredential = GoogleIdTokenCredential.Companion.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken

                    if (idToken != null) {
                        signInWithFirebase(idToken)
                    } else {
                        authViewModel.onGoogleSignInFails("Failed to get ID Token")
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
                    Toast.makeText(requireContext(), "Sign in successful", Toast.LENGTH_SHORT).show()
                } else {
                    authViewModel.onGoogleSignInFails("Firebase login failed: ${task.exception?.message}")
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}