package com.example.notess.domain.use_case.note

import com.example.notess.data.model.Note
import com.example.notess.data.model.SyncAction
import com.example.notess.data.repository.NoteRepository
import javax.inject.Inject

class RestoreNoteUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(note: Note) {
        val restoreNote = note.copy(
            isDeleted = false,
            needsSync = true,
            updatedAt = System.currentTimeMillis(),
            syncAction = SyncAction.UPDATE
        )
        repository.updateNote(restoreNote)
    }
}