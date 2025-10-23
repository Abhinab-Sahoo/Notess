package com.example.notess.domain.model

import com.example.notess.domain.use_case.note.AddNoteUseCase
import com.example.notess.domain.use_case.note.ArchiveNoteUseCase
import com.example.notess.domain.use_case.note.DeleteNoteForeverUseCase
import com.example.notess.domain.use_case.note.GetNoteUseCase
import com.example.notess.domain.use_case.note.MoveToTrashUseCase
import com.example.notess.domain.use_case.note.SaveAndArchiveNoteUseCase
import com.example.notess.domain.use_case.note.UnarchiveNoteUseCase
import com.example.notess.domain.use_case.note.UpdateNoteUseCase
import javax.inject.Inject

data class NoteActionsUseCases @Inject constructor(
    val addNote: AddNoteUseCase,
    val updateNote: UpdateNoteUseCase,
    val saveAndArchive: SaveAndArchiveNoteUseCase,
    val getNote: GetNoteUseCase,
    val archiveNote: ArchiveNoteUseCase,
    val unarchiveNote: UnarchiveNoteUseCase,
    val moveToTrash: MoveToTrashUseCase,
    val deleteForever: DeleteNoteForeverUseCase
)
