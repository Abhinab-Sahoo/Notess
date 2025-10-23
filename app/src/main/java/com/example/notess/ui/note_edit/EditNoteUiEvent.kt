package com.example.notess.ui.note_edit

sealed class EditNoteUiEvent {
    data class ShowConfirmationAndNavigateUp(val message: String) : EditNoteUiEvent()
}