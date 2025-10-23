package com.example.notess.domain.use_case.note

import com.example.notess.data.model.Note
import com.example.notess.data.model.SyncAction
import com.example.notess.data.repository.NoteRepository
import javax.inject.Inject

class DeleteAllTrashedNotesUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke() : List<Note> {
        val allTrashedNotes = repository.getTrashedNotesForDeletion()

        val notesToDelete = allTrashedNotes.map { note ->
            note.copy(syncAction = SyncAction.DELETE)
        }
        repository.deleteAllTrashedNotes(notesToDelete)
        return notesToDelete
    }
}