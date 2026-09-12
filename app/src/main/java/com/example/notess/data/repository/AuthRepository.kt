package com.example.notess.data.repository

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val credentialManager: CredentialManager
) {
    private val webClientId = "268067917979-l377tff12vcto0kba0bnir226vttr0nb.apps.googleusercontent.com"

    fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser

    suspend fun singInWithGoogle(activityContext: Context): FirebaseUser {
        val rawIdToken = fetchGoogleIdToken(activityContext)
        return authenticateWithFirebase(rawIdToken)
    }

    private suspend fun fetchGoogleIdToken(activityContext: Context): String {
        val googleIdOption = GetSignInWithGoogleOption.Builder(webClientId).build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result = credentialManager.getCredential(
            context = activityContext,
            request = request
        )

        val credential = result.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            return GoogleIdTokenCredential.createFrom(credential.data).idToken
        } else {
            throw Exception("Invalid credential type: ${credential.type}")
        }

    }

    private suspend fun authenticateWithFirebase(idToken: String): FirebaseUser {
        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
        val authResult = firebaseAuth.signInWithCredential(firebaseCredential).await()

        return authResult.user ?: throw Exception("Firebase authentication returned an empty user profile")
    }

    suspend fun signOut() {
        firebaseAuth.signOut()
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }
}