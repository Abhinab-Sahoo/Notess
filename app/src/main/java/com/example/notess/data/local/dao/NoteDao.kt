package com.example.notess.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.notess.data.model.Note


@Dao
interface NoteDao {

    @Query("SELECT * FROM note_database ORDER BY id DESC")
    fun getNotes() : LiveData<List<Note>>

    @Query("SELECT * FROM note_database WHERE id = :id")
    fun getNote(id: Int) : LiveData<Note>

    @Query("SELECT * FROM note_database WHERE (:query IS NULL OR :query = '' OR LOWER(noteHead) LIKE LOWER(:query) OR LOWER(noteBody) LIKE LOWER(:query)) ORDER BY id DESC")
    fun searchNote(query: String?): LiveData<List<Note>>

    @Query("SELECT * FROM note_database WHERE isArchived = 0 AND isDeleted = 0 ORDER BY id DESC")
    fun getActiveNotes(): LiveData<List<Note>>

    @Query("SELECT * FROM note_database WHERE isArchived = 1 AND isDeleted = 0 ORDER BY id DESC")
    fun getArchivedNotes(): LiveData<List<Note>>

    @Query("SELECT * FROM note_database WHERE isDeleted = 1 ORDER BY id DESC")
    fun getTrashedNotes(): LiveData<List<Note>>

    @Query("DELETE FROM note_database WHERE isDeleted = 1")
    suspend fun deleteAllTrashedNotes()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    // New sync related queries

    @Query("SELECT * FROM note_database WHERE needsSync = 1")
    suspend fun getNotesNeedingSync(): List<Note>

    @Query("UPDATE note_database SET needsSync = 0, firebaseId = :firebaseId WHERE id = :noteId")
    suspend fun markAsSynced(noteId: Int, firebaseId: String)

    @Query("SELECT * FROM note_database WHERE isDeleted = 1")
    suspend fun getAllTrashedNote(): List<Note>
}