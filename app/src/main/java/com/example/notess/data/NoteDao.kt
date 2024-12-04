package com.example.notess.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.notess.model.Note


@Dao
interface NoteDao {

    @Query("SELECT * FROM note_database ORDER BY id DESC")
    fun getNotes() : LiveData<List<Note>>

    @Query("SELECT * FROM note_database WHERE id = :id")
    fun getNote(id: Int) : LiveData<Note>

    @Query("SELECT * FROM note_database WHERE (:query IS NULL OR :query = '' OR LOWER(noteHead) LIKE LOWER(:query) OR LOWER(noteBody) LIKE LOWER(:query)) ORDER BY id DESC")
    fun searchNote(query: String?): LiveData<List<Note>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)
}