package com.example.notess.domain.use_case.note

import com.example.notess.data.model.Note
import com.example.notess.data.model.SyncAction
import com.example.notess.data.repository.NoteRepository
import javax.inject.Inject

class ArchiveNoteUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(note: Note) {

        if (note.isArchived) return

        val archivedNote = note.copy(
            isArchived = true,
            needsSync = true,
            updatedAt = System.currentTimeMillis(),
            syncAction = SyncAction.UPDATE
        )
        repository.updateNote(archivedNote)

    }
}