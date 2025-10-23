package com.example.notess.domain.use_case.note

import com.example.notess.data.model.Note
import com.example.notess.data.model.SyncAction
import com.example.notess.data.repository.NoteRepository
import javax.inject.Inject

class UnarchiveNoteUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(note: Note) {
        if (!note.isArchived) return

        val unarchivedNote = note.copy(
            isArchived = false,
            needsSync = true,
            updatedAt = System.currentTimeMillis(),
            syncAction = SyncAction.UPDATE
        )
        repository.updateNote(unarchivedNote)
    }
}