package com.example.notess.data.repository

import androidx.lifecycle.LiveData
import com.example.notess.data.local.dao.NoteDao
import com.example.notess.data.model.Note
import javax.inject.Inject

class NoteRepository @Inject constructor(private val noteDao: NoteDao) {

    val activeNotes: LiveData<List<Note>> = noteDao.getActiveNotes()
    val archivedNotes: LiveData<List<Note>> = noteDao.getArchivedNotes()
    val trashedNotes: LiveData<List<Note>> = noteDao.getTrashedNotes()

    fun searchNote(query: String?): LiveData<List<Note>> {
        val formattedQuery = if (query.isNullOrBlank()) "" else "%${query.trim()}%"
        return noteDao.searchNote(formattedQuery)
    }

    fun getNote(id: Int): LiveData<Note> = noteDao.getNote(id)

    suspend fun insertNote(note: Note) = noteDao.insertNote(note)
    suspend fun updateNote(note: Note) = noteDao.updateNote(note)
    suspend fun deleteNote(note: Note) = noteDao.deleteNote(note)
    suspend fun deleteAllTrashedNotes() = noteDao.deleteAllTrashedNotes()

    suspend fun archiveNote(note: Note) {
        val archivedNote = note.copy(isArchived = true)
        noteDao.updateNote(archivedNote)
    }

    suspend fun unArchiveNote(note: Note) {
        val unarchivedNote = note.copy(isArchived = false)
        noteDao.updateNote(unarchivedNote)
    }

    suspend fun moveToTrash(note: Note, source: String) {
        val trashedNote = note.copy(
            isArchived = false,
            isDeleted = true,
            deletedAt = System.currentTimeMillis(),
            deletedFrom = source
        )
        noteDao.updateNote(trashedNote)
    }

    suspend fun restoreNote(note: Note) {
        val restoredNote = note.copy(
            isDeleted = false,
            isArchived = note.deletedFrom == "archive",
            deletedAt = null,
            deletedFrom = null
        )
        noteDao.updateNote(restoredNote)
    }
}
