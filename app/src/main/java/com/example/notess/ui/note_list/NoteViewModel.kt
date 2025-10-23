package com.example.notess.ui.note_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.notess.data.model.Note
import com.example.notess.data.repository.NoteRepository
import com.example.notess.data.repository.UserPreferencesRepository
import com.example.notess.domain.model.NoteValidationResult
import com.example.notess.domain.use_case.note.AddNoteUseCase
import com.example.notess.domain.use_case.note.ArchiveNoteUseCase
import com.example.notess.domain.use_case.note.DeleteAllTrashedNotesUseCase
import com.example.notess.domain.use_case.note.MoveToTrashUseCase
import com.example.notess.domain.use_case.note.SaveAndArchiveNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(
    private var repository: NoteRepository,
    private val addNoteUseCase: AddNoteUseCase,
    private val saveAndArchiveNoteUseCase: SaveAndArchiveNoteUseCase,
    private val archiveNoteUseCase: ArchiveNoteUseCase,
    private val moveToTrashUseCase: MoveToTrashUseCase,
    private val deleteAllTrashedNotesUseCase: DeleteAllTrashedNotesUseCase,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val activeNotes: LiveData<List<Note>> = repository.activeNotes
    val archivedNotes: LiveData<List<Note>> = repository.archivedNotes
    val trashedNotes: LiveData<List<Note>> = repository.trashedNotes
    private val _searchQuery = MutableStateFlow<String?>(null)
    val notes: LiveData<List<Note>> = _searchQuery
        .debounce(300)
        .asLiveData()
        .switchMap { query ->
            if (query.isNullOrBlank()) {
                activeNotes
            } else { repository.searchNote(query) }
        }
    private val _screenState = MutableStateFlow(NoteListScreenState())
    val screenState: StateFlow<NoteListScreenState> = _screenState.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    init {
        viewModelScope.launch {
            val initialLayoutIsGrid = userPreferencesRepository
                .layoutPreference.first()
            _screenState.update {
                it.copy(isGridLayout = initialLayoutIsGrid)
            }
        }
    }

    sealed class UiEvent {
        object NavigateBack : UiEvent()
        object ShowEmptyNoteError : UiEvent()
        object ShowArchiveConfirmationAndNavigateBack : UiEvent()
        data class ShowToast(val message: String) : UiEvent()
    }

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun onNoteSwipedRight(note: Note) {
        viewModelScope.launch {
            archiveNoteUseCase(note)
            _uiEvent.send(
                UiEvent.ShowToast("Note Archived!")
            )
        }
    }
    fun onNoteSwipedLeft(note: Note) {
        viewModelScope.launch {
            moveToTrashUseCase(note)
            _uiEvent.send(
                UiEvent.ShowToast("Moved to Bin!")
            )
        }
    }

    fun addNote(noteHead: String?, noteBody: String) {
        viewModelScope.launch {
            when (addNoteUseCase(noteHead, noteBody)) {
                NoteValidationResult.SUCCESS -> _uiEvent.send(UiEvent.NavigateBack)
                NoteValidationResult.EMPTY_NOTE -> _uiEvent.send(UiEvent.ShowEmptyNoteError)
            }
        }
    }

    fun onSaveAndArchiveClicked(
        noteHead: String?, noteBody: String
    ) {
        viewModelScope.launch {
            when (saveAndArchiveNoteUseCase(noteHead, noteBody)) {
                NoteValidationResult.SUCCESS -> _uiEvent.send(UiEvent.ShowArchiveConfirmationAndNavigateBack)
                NoteValidationResult.EMPTY_NOTE -> _uiEvent.send(UiEvent.ShowEmptyNoteError)
            }
        }
    }

    fun onDeleteAllTrashedNotesClicked() {
        viewModelScope.launch {
            val notesToDeleteOnServer = deleteAllTrashedNotesUseCase()
            _uiEvent.send(UiEvent.ShowToast("Trash Emptied!"))
        }
    }

    fun onFabClicked() {
        _screenState.update { currentState ->
            currentState.copy(isFabMenuOpen = !currentState.isFabMenuOpen)
        }
    }
    fun closeFabMenu() {
        _screenState.update {
            it.copy(isFabMenuOpen = false)
        }
    }

    fun onLayoutToggleClicked() {
        viewModelScope.launch {
            val newLayoutIsGrid = !_screenState.value.isGridLayout
            userPreferencesRepository.saveLayoutPreference(newLayoutIsGrid)
            _screenState.update { it.copy(isGridLayout = newLayoutIsGrid) }
        }
    }

}