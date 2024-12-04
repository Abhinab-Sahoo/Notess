package com.example.notess.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "note_database")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val noteHead: String,
    val noteBody: String
)
