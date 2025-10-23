package com.example.notess.domain.use_case.note

import com.example.notess.data.model.Note
import com.example.notess.data.model.SyncAction
import com.example.notess.data.repository.NoteRepository
import com.example.notess.domain.model.NoteValidationResult
import javax.inject.Inject

class AddNoteUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(noteHead: String?, noteBody: String): NoteValidationResult {

        if (!noteHead.isNullOrBlank() || noteBody.isNotBlank()) {
            val note = Note(
                noteHead = noteHead ?: "",
                noteBody = noteBody,
                needsSync = true,
                syncAction = SyncAction.CREATE,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            repository.insertNote(note)
            // 3. Return a success result
            return NoteValidationResult.SUCCESS
        } else {
            // 4. If the condition is false, it means the note is empty. Return an error result.
            return NoteValidationResult.EMPTY_NOTE
        }
    }
}