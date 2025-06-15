package com.example.notess.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.notess.data.model.Note
import com.example.notess.data.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class NoteViewModel @Inject constructor(
    private var repository: NoteRepository
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
            noteBody = noteBody
        )

        viewModelScope.launch(Dispatchers.IO) {
            repository.insertNote(note)
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateNote(note)
        }
    }

    fun archiveNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.archiveNote(note)
        }
    }

    fun unArchiveNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.unArchiveNote(note)
        }
    }

    fun insertNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertNote(note)
        }
    }

    fun moveToTrash(note: Note, source: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.moveToTrash(note, source)
        }
    }

    fun deleteAllTrashedNotes() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllTrashedNotes()
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteNote(note)
        }
    }

    fun restoreNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.restoreNote(note)
        }
    }

}