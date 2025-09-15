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

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {

        // Add createdAt column - default to current time
        db.execSQL("ALTER TABLE note_database ADD COLUMN createdAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}")

        // Add updatedAt column - default to current time
        db.execSQL("ALTER TABLE note_database ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}")

        // Add needsSync column - default to 1 (true) so existing notes will be synced
        db.execSQL("ALTER TABLE note_database ADD COLUMN needsSync INTEGER NOT NULL DEFAULT 1")

        // Add firebaseId column - default to null (no Firebase ID yet)
        db.execSQL("ALTER TABLE note_database ADD COLUMN firebaseId TEXT")
    }
}