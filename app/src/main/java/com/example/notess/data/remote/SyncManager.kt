package com.example.notess.data.remote

import android.util.Log
import com.example.notess.data.model.Note
import com.example.notess.data.repository.NoteRepository
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class SyncManager @Inject constructor(
    private val auth: FirebaseAuth,
    private val noteRepository: NoteRepository,
    private val firebaseDataSource: FirebaseDataSource
) {

     suspend fun syncPendingNotes() {

        // if user not logged in exit function with null
        if (auth.currentUser == null) return

        val notesNeedingSync = noteRepository.getNotesNeedingSync()

        notesNeedingSync.forEach { note ->
            uploadSingleNote(note)
        }
    }

    private suspend fun uploadSingleNote(note: Note) {

        try {
            if (note.firebaseId == null) {
                val firebaseId = firebaseDataSource.uploadNoteToFirebase(note)

                if (firebaseId != null) {
                    noteRepository.markNoteAsSynced(note.id, firebaseId)
                }
            } else {
                firebaseDataSource.updateNoteInFirebase(note)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun performInitialSync() {
        syncPendingNotes()
    }

    suspend fun syncSingleNoteIfLoggedIn(note: Note) {
        if (auth.currentUser != null)
            uploadSingleNote(note)
    }

    suspend fun deleteAllTrashedNotesFromFirestore(trashedNotes: List<Note>) {
        if (auth.currentUser != null) {
            firebaseDataSource.deleteAllTrashedNotesFromFirebase(trashedNotes)
        }
    }

}