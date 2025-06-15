package com.example.notess.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "note_database")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val noteHead: String,
    val noteBody: String,
    val isArchived: Boolean = false,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val deletedFrom: String? = null
)
