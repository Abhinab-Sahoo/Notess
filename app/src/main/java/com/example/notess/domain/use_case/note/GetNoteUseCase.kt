package com.example.notess.domain.use_case.note

import com.example.notess.data.model.Note
import com.example.notess.data.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNoteUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    operator fun invoke(id: Int) : Flow<Note?> {
        return repository.getNote(id)
    }
}