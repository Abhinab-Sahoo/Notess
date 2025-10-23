package com.example.notess.ui.note_list

/**
 * Represents the state of the Note List screen.
 * This is the blueprint that the ViewModel provides to the Fragment.
 */
data class NoteListScreenState(
    val isFabMenuOpen: Boolean = false,
    val isGridLayout: Boolean = true
)
