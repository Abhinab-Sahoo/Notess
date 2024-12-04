package com.example.notess.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.notess.data.NoteDao
import com.example.notess.data.NoteDatabase
import com.example.notess.model.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteViewModel(private var noteDao: NoteDao) : ViewModel() {

    val allNotes: LiveData<List<Note>> = noteDao.getNotes()
    private val _searchQuery = MutableLiveData<String?>()

    val searchResults: LiveData<List<Note>> = _searchQuery.switchMap { query ->
        noteDao.searchNote(if (query.isNullOrBlank()) "" else "%$query%")
    }

    fun retrieveNote(id: Int) : LiveData<Note> {
        return noteDao.getNote(id)
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
            noteDao.insertNote(note)
        }
    }

    fun updateNote(
        id: Int,
        noteHead: String?,
        noteBody: String
    ) {
        val note = Note(
            id = id,
            noteHead = noteHead ?: "",
            noteBody = noteBody
        )

        viewModelScope.launch(Dispatchers.IO) {
            noteDao.updateNote(note)
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.deleteNote(note)
        }
    }

    fun isEntryValid(noteHead: String = " ", noteBody: String) : Boolean {
        return noteHead.isNotBlank() || noteBody.isNotBlank()
    }
}

class NoteViewModelFactory(private val noteDao: NoteDao) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NoteViewModel(noteDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}











