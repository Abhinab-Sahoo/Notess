package com.example.notess.data.remote

import com.example.notess.data.model.Note
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    suspend fun uploadNoteToFirebase(note: Note) : String? {

        // get the current logged in user
        val userId = auth.currentUser?.uid ?: return null

        val noteData = mapOf(
            "noteHead" to note.noteHead,
            "noteBody" to note.noteBody,
            "isArchived" to note.isArchived,
            "isDeleted" to note.isDeleted,
            "deletedAt" to note.deletedAt,
            "deletedFrom" to note.deletedFrom,
            "createdAt" to note.createdAt,
            "updatedAt" to note.updatedAt,
        )

        return try {
            val documentRef = firestore
                .collection("users")
                .document(userId)
                .collection("notes")
                .add(noteData)
                .await()

            documentRef.id
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateNoteInFirebase(note: Note): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        val firebaseId = note.firebaseId ?: return false

        val noteData = mapOf(
            "noteHead" to note.noteHead,
            "noteBody" to note.noteBody,
            "isArchived" to note.isArchived,
            "isDeleted" to note.isDeleted,
            "deletedAt" to note.deletedAt,
            "deletedFrom" to note.deletedFrom,
            "createdAt" to note.createdAt,
            "updatedAt" to note.updatedAt,
        )

        return try {
            firestore
                .collection("users")
                .document(userId)
                .collection("notes")
                .document(firebaseId)
                .set(noteData)
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteAllTrashedNotesFromFirebase(trashedNotes: List<Note>) : Boolean {
        val userId = auth.currentUser?.uid ?: return false

        return try {
            val syncedNotes = trashedNotes.filter { !it.firebaseId.isNullOrEmpty() }
            val deleteBatch = firestore.batch()

            if (syncedNotes.isEmpty()) return true

            syncedNotes.forEach { note ->
                note.firebaseId?.let { firebaseId ->
                    val docRef = firestore
                        .collection("users")
                        .document(userId)
                        .collection("notes")
                        .document(firebaseId)
                    deleteBatch.delete(docRef)
                }
            }
            deleteBatch.commit().await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}