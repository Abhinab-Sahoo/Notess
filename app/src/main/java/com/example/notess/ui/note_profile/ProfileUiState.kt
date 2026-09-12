package com.example.notess.ui.note_profile

import com.google.firebase.auth.FirebaseUser

data class ProfileUiState(
    val isLoading: Boolean = false,
    val user: FirebaseUser? = null,
    val errorMessage: String? = null
)
