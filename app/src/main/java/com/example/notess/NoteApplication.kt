package com.example.notess

import android.app.Application
import com.example.notess.data.NoteDatabase

class NoteApplication : Application() {

    val database: NoteDatabase by lazy {
        NoteDatabase.getDatabase(this)
    }
}