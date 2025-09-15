package com.example.notess.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notess.data.remote.SyncManager
import com.example.notess.data.repository.NoteRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val noteRepository: NoteRepository,
    private val syncManager: SyncManager
) : ViewModel() {

    // LiveData to observe login status
    private var _isUserLoggedIn = MutableLiveData<Boolean>()
    val isUserLoggedIn: LiveData<Boolean> = _isUserLoggedIn

    // LiveData to observe sync status
    private var _syncStatus = MutableLiveData<String>()
    val syncStatus: LiveData<String> = _syncStatus

    // Track if initial sync is in progress
    private var _isSyncing = MutableLiveData<Boolean>()
    val isSyncing: LiveData<Boolean> = _isSyncing

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        _isUserLoggedIn.value = firebaseAuth.currentUser != null
    }

    init {
        _isUserLoggedIn.value = auth.currentUser != null
        auth.addAuthStateListener(authStateListener)
        checkAuthState()
    }

    private fun checkAuthState() {
        val currentUser = auth.currentUser
        _isUserLoggedIn.value = currentUser != null

        if ( currentUser != null) {
            _syncStatus.value = "User logged in as ${currentUser.email}"
        } else {
            _syncStatus.value = "User not logged in"
        }
    }

    fun onGoogleSignInSuccess() {
        _isUserLoggedIn.value = true
        _syncStatus.value = "Sign-in successful! Starting sync"
        _isSyncing.value = true

        viewModelScope.launch {
            try {
                syncManager.performInitialSync()
                _syncStatus.value = "All notes synced successfully!"
                _isSyncing.value = false
            } catch (e: Exception) {
                _syncStatus.value = "Syncing failed: ${e.message}"
                _isSyncing.value = false
            }
        }
    }

    fun onGoogleSignInFails(error: String) {
        _isUserLoggedIn.value = false
        _syncStatus.value = "Sign in failed $error"
        _isSyncing.value = false
    }

    fun signOut() {
        auth.signOut()
        _isUserLoggedIn.value = false
        _syncStatus.value = "Signed out successfully"
        _isSyncing.value = false
    }

    fun manualSync() {
        if (auth.currentUser == null) {
            _syncStatus.value = "Please sign in first to sync"
            return
        }

        _syncStatus.value = "Starting manual sync"
        _isSyncing.value = true

        viewModelScope.launch {
            try {
                syncManager.performInitialSync()
                _syncStatus.value = "Manual sync completed!"
                _isSyncing.value = false
            } catch (e: Exception) {
                _syncStatus.value = "Manual sync failed: ${e.message}"
                _isSyncing.value = false
            }
        }
    }
}