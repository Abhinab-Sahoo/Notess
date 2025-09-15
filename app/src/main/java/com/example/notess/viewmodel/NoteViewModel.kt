package com.example.notess.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.notess.data.model.Note
import com.example.notess.data.remote.SyncManager
import com.example.notess.data.repository.NoteRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class NoteViewModel @Inject constructor(
    private var repository: NoteRepository,
    private val syncManager: SyncManager,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _searchQuery = MutableLiveData<String?>()

    val searchResults: LiveData<List<Note>> = _searchQuery.switchMap { query ->
        repository.searchNote(query)
    }

    val activeNotes: LiveData<List<Note>> = repository.activeNotes
    val archivedNotes: LiveData<List<Note>> = repository.archivedNotes
    val trashedNotes: LiveData<List<Note>> = repository.trashedNotes

    fun retrieveNote(id: Int): LiveData<Note> {
        return repository.getNote(id)
    }

    fun updateSearchQuery(query: String?) {
        _searchQuery.value = query
    }

    fun addNote(
        noteHead: String?,
        noteBody: String
    ) {
        val note = Note(
            noteHead = noteHead ?: "",
            noteBody = noteBody,
            needsSync = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        viewModelScope.launch(Dispatchers.IO) {

            repository.insertNote(note)
//            syncManager.syncSingleNoteIfLoggedIn(note)
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedNote = note.copy(
                updatedAt = System.currentTimeMillis(),
                needsSync = true
            )
            repository.updateNote(updatedNote)

            syncManager.syncSingleNoteIfLoggedIn(updatedNote)
        }
    }

    fun saveAndArchiveNote(noteHead: String, noteBody: String) {
        val currentTime = System.currentTimeMillis()
        val note = Note(
            noteHead = noteHead,
            noteBody = noteBody,
            isArchived = true,
            needsSync = true,
            createdAt = currentTime,
            updatedAt = currentTime
        )
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertNote(note)
            syncManager.syncSingleNoteIfLoggedIn(note)
        }
    }

    fun archiveNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentTime = System.currentTimeMillis()
            val archivedNote = note.copy(
                isArchived = true,
                updatedAt = currentTime,
                needsSync = true
            )
            repository.archiveNote(archivedNote)
            syncManager.syncSingleNoteIfLoggedIn(archivedNote)
        }
    }

    fun unArchiveNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentTime = System.currentTimeMillis()
            val unarchivedNote = note.copy(
                isArchived = false,
                updatedAt = currentTime,
                needsSync = true
            )
            repository.unArchiveNote(unarchivedNote)
            syncManager.syncSingleNoteIfLoggedIn(unarchivedNote)
        }
    }

    fun insertNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertNote(note)
        }
    }

    fun moveToTrash(note: Note, source: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentTime = System.currentTimeMillis()
            val trashedNote = note.copy(
                isDeleted = true,
                isArchived = false,
                deletedAt = currentTime,
                deletedFrom = source,
                updatedAt = currentTime,
                needsSync = true
            )
            repository.moveToTrash(trashedNote)
            syncManager.syncSingleNoteIfLoggedIn(trashedNote)
        }
    }

    fun deleteAllTrashedNotes() {
        viewModelScope.launch(Dispatchers.IO) {
            val trashedNotes = repository.deleteAllTrashedNotes()
            syncManager.deleteAllTrashedNotesFromFirestore(trashedNotes)
            repository.deleteAllTrashedNotes()
        }
    }

    fun deleteNoteForever(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            val trashedNotes = repository.deleteAllTrashedNotes()
            syncManager.deleteAllTrashedNotesFromFirestore(trashedNotes)
            repository.deleteNote(note)
        }
    }

    fun restoreNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.restoreNote(note)
            syncManager.syncSingleNoteIfLoggedIn(note)
        }
    }

}