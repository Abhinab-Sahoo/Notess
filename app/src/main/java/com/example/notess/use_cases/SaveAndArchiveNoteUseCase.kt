package com.example.notess.use_cases

import com.example.notess.data.model.Note
import com.example.notess.data.model.SyncAction
import com.example.notess.data.repository.NoteRepository
import javax.inject.Inject

class SaveAndArchiveNoteUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(
        noteHead: String?, noteBody: String
    ) : AddNoteResult {

        val now = System.currentTimeMillis()

        if (!noteHead.isNullOrBlank() || noteBody.isNotBlank()) {
            val note = Note(
                noteHead = noteHead ?: "",
                noteBody = noteBody,
                isArchived = true,
                needsSync = true,
                syncAction = SyncAction.CREATE,
                createdAt = now,
                updatedAt = now
            )
            repository.insertNote(note)
            return AddNoteResult.SUCCESS
        } else {
            return AddNoteResult.EMPTY_NOTE
        }
    }
}