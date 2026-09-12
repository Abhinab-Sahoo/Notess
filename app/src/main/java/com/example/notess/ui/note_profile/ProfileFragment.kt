package com.example.notess.ui.note_profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.notess.R
import com.example.notess.databinding.FragmentProfileBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val profileViewModel: ProfileViewModel by viewModels()

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

        // Update UI
        observeUiState()
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                profileViewModel.uiState.collect { state ->

                    binding.progressBar.isVisible = state.isLoading

                    if (state.user != null) {
                        binding.signInAndOutTextView.text = "Sign Out"
                        binding.SignInAndOutButton.setImageResource(R.drawable.logout)
                        binding.displayName.text = state.user.displayName ?: "No Name"
                        binding.email.text = state.user.email
                        binding.deleteAccountCardView.visibility = View.VISIBLE
                    } else {
                        binding.signInAndOutTextView.text = "Sign In"
                        binding.SignInAndOutButton.setImageResource(R.drawable.login)
                        binding.displayName.text = "Guest"
                        binding.email.text = "Not signed in"
                        binding.deleteAccountCardView.visibility = View.GONE
                    }

                    state.errorMessage?.let { msg ->
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                        profileViewModel.errorShown()
                    }
                }
            }
        }
    }

    private fun setupSignInOutButton() {
        binding.signInAndOutCardView.setOnClickListener {
            val isUserLoggedIn = profileViewModel.uiState.value.user != null

            if (isUserLoggedIn) {
                // User is logged in - sign them out
                profileViewModel.signOut()
                findNavController().navigate(R.id.noteFragment)
            } else {
                // User is not logged in - sign them in
                profileViewModel.handleGoogleSignIn(requireActivity())
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}