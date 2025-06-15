package com.example.notess.data.local.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Migration 2 -> 3: Added isDeleted (Int) and deletedAt (Long) Column for soft delete functionality.
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE note_database ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE note_database ADD COLUMN deletedAt INTEGER")
    }
}

// Migration 3 -> 4: Added deletedFrom Column for soft delete functionality.
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE note_database ADD COLUMN deletedFrom TEXT")
    }
}