package com.example.notess.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.notess.data.local.converters.Converters
import com.example.notess.data.local.dao.NoteDao
import com.example.notess.data.model.Note

@TypeConverters(Converters::class)
@Database(entities = [Note::class], version = 6, exportSchema = false)
abstract class NoteDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao
}