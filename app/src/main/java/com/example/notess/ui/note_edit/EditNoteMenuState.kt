package com.example.notess.ui.note_edit

data class EditNoteMenuState(
    val showSaveButton: Boolean = false,
    val showArchiveMenuButton: Boolean = false,
    val showUnarchiveMenuButton: Boolean = false,
    val showTrashButton: Boolean = false,
    val showRestoreButton: Boolean = false,
    val showDeleteForeverButton: Boolean = false
)