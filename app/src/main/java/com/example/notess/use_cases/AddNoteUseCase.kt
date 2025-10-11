package com.example.notess.use_cases

import com.example.notess.data.model.Note
import com.example.notess.data.model.SyncAction
import com.example.notess.data.repository.NoteRepository
import javax.inject.Inject

class AddNoteUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(noteHead: String?, noteBody: String): AddNoteResult {

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
            return AddNoteResult.SUCCESS
        } else {
            // 4. If the condition is false, it means the note is empty. Return an error result.
            return AddNoteResult.EMPTY_NOTE
        }
    }
}
