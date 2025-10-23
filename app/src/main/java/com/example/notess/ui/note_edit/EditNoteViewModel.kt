package com.example.notess.ui.note_edit

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.notess.data.model.Note
import com.example.notess.domain.use_case.note.ArchiveNoteUseCase
import com.example.notess.domain.use_case.note.DeleteNoteForeverUseCase
import com.example.notess.domain.use_case.note.GetNoteUseCase
import com.example.notess.domain.use_case.note.MoveToTrashUseCase
import com.example.notess.domain.use_case.note.RestoreNoteUseCase
import com.example.notess.domain.use_case.note.UnarchiveNoteUseCase
import com.example.notess.domain.use_case.note.UpdateNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditNoteViewModel @Inject constructor(
    getNoteUseCase: GetNoteUseCase,
    private val archiveNoteUseCase: ArchiveNoteUseCase,
    private val updateNoteUseCase: UpdateNoteUseCase,
    private val unarchiveNoteUseCase: UnarchiveNoteUseCase,
    private val moveToTrashUseCase: MoveToTrashUseCase,
    private val restoreNoteUseCase: RestoreNoteUseCase,
    private val deleteNoteForeverUseCase: DeleteNoteForeverUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val noteId: Int = savedStateHandle["id"] ?: 0
    val note: LiveData<Note?> = getNoteUseCase(noteId).asLiveData()

    private val _menuScreenState = MutableStateFlow(EditNoteMenuState())
    val menuScreenState: StateFlow<EditNoteMenuState> = _menuScreenState.asStateFlow()

    private val _uiEvent = Channel<EditNoteUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun onNoteLoaded(note: Note) {
        val isNoteDeleted = note.isDeleted
        val isNoteArchived = note.isArchived

        _menuScreenState.value = EditNoteMenuState(
            showSaveButton = !isNoteDeleted,
            showArchiveMenuButton = !isNoteArchived && !isNoteDeleted,
            showUnarchiveMenuButton = isNoteArchived && !isNoteDeleted,
            showTrashButton = !isNoteDeleted,
            showRestoreButton = isNoteDeleted,
            showDeleteForeverButton = isNoteDeleted
        )
    }

    fun onSaveClicked(note: Note, newTitle: String, newBody: String) {
        viewModelScope.launch {
            updateNoteUseCase(note, newTitle, newBody)
            _uiEvent.send(
                EditNoteUiEvent
                    .ShowConfirmationAndNavigateUp("Note Updated!")
            )
        }
    }

    fun onArchiveClicked(note: Note) {
        viewModelScope.launch {
            archiveNoteUseCase(note)
            _uiEvent.send(
                EditNoteUiEvent
                    .ShowConfirmationAndNavigateUp("Note Archived!")
            )
        }
    }

    fun onUnarchiveClicked(note: Note) {
        viewModelScope.launch {
            unarchiveNoteUseCase(note)
            _uiEvent.send(
                EditNoteUiEvent
                    .ShowConfirmationAndNavigateUp("Note Unarchived")
            )
        }
    }

    fun onTrashClicked(note: Note) {
        viewModelScope.launch {
            moveToTrashUseCase(note)
            _uiEvent.send(
                EditNoteUiEvent
                    .ShowConfirmationAndNavigateUp("Moved To Bin")
            )
        }
    }
    fun onRestoreClicked(note: Note) {
        viewModelScope.launch {
            restoreNoteUseCase(note)
            _uiEvent.send(
                EditNoteUiEvent
                    .ShowConfirmationAndNavigateUp("Note Restored")
            )
        }
    }
    fun onDeleteForeverClicked(note: Note) {
        viewModelScope.launch {
            deleteNoteForeverUseCase(note)
            _uiEvent.send(
                EditNoteUiEvent
                    .ShowConfirmationAndNavigateUp("Note Deleted Forever")
            )
        }
    }

}