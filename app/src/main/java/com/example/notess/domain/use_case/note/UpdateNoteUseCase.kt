package com.example.notess.domain.use_case.note

import com.example.notess.data.model.Note
import com.example.notess.data.model.SyncAction
import com.example.notess.data.repository.NoteRepository
import com.example.notess.domain.model.NoteValidationResult
import javax.inject.Inject

class UpdateNoteUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    suspend operator fun invoke(
        note: Note, newTitle: String, newBody: String
    ) : NoteValidationResult {
        if (newTitle.isBlank() && newBody.isBlank()) {
            return NoteValidationResult.EMPTY_NOTE
        }

        val updatedNote = note.copy(
            noteHead = newTitle,
            noteBody = newBody,
            updatedAt = System.currentTimeMillis(),
            needsSync = true,
            syncAction = SyncAction.UPDATE
        )
        noteRepository.updateNote(updatedNote)
        return NoteValidationResult.SUCCESS
    }
}