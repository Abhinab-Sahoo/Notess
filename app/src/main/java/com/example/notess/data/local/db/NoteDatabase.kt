package com.example.notess.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.notess.data.local.dao.NoteDao
import com.example.notess.data.model.Note

@Database(entities = [Note::class], version = 4, exportSchema = false)
abstract class NoteDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao
}