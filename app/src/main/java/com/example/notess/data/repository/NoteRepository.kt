package com.example.notess.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.notess.data.local.dao.NoteDao
import com.example.notess.data.model.Note
import com.example.notess.worker.SyncNotesWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import javax.inject.Inject

class NoteRepository @Inject constructor(
    private val noteDao: NoteDao,
    @ApplicationContext private val context: Context
) {

    val activeNotes: LiveData<List<Note>> = noteDao.getActiveNotes()
    val archivedNotes: LiveData<List<Note>> = noteDao.getArchivedNotes()
    val trashedNotes: LiveData<List<Note>> = noteDao.getTrashedNotes()

    fun searchNote(query: String?): LiveData<List<Note>> {
        val formattedQuery = if (query.isNullOrBlank()) "" else "%${query.trim()}%"
        return noteDao.searchNote(formattedQuery)
    }

    fun getNote(id: Int): LiveData<Note> = noteDao.getNote(id)

//    suspend fun getAllNotes(): LiveData<List<Note>> = noteDao.getNotes()

    suspend fun insertNote(note: Note) {
        noteDao.insertNote(note)
        scheduleNoteSync()
    }

    suspend fun updateNote(note: Note) {
        val updatedNote = note.copy(
            needsSync = true
        )
        noteDao.updateNote(updatedNote)
    }
    suspend fun deleteNote(note: Note) = noteDao.deleteNote(note)

    suspend fun archiveNote(note: Note) {
        noteDao.updateNote(note)
    }

    suspend fun unArchiveNote(note: Note) {
        noteDao.updateNote(note)
    }

    suspend fun moveToTrash(note: Note) {
        noteDao.updateNote(note)
    }

    suspend fun restoreNote(note: Note) {
        val restoredNote = note.copy(
            isDeleted = false,
            isArchived = note.deletedFrom == "archive",
            deletedAt = null,
            deletedFrom = null,
            updatedAt = System.currentTimeMillis(),
            needsSync = true
        )
        noteDao.updateNote(restoredNote)
    }

    // New firebase sync related functions

    suspend fun getNotesNeedingSync(): List<Note> {
        return noteDao.getNotesNeedingSync()
    }

    suspend fun markNoteAsSynced(noteId: Int, firebaseId: String) {
        return noteDao.markAsSynced(noteId, firebaseId)
    }

    suspend fun deleteAllTrashedNotes(): List<Note> {
        return noteDao.getAllTrashedNote()
    }

    private fun scheduleNoteSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<SyncNotesWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }

}
