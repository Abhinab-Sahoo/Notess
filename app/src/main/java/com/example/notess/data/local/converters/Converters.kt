package com.example.notess.data.local.converters

import androidx.room.TypeConverter
import com.example.notess.data.model.SyncAction

class Converters {

    @TypeConverter
    fun fromSyncAction(syncAction: SyncAction) : String {
        return syncAction.name
    }

    @TypeConverter
    fun toSyncAction(value: String) : SyncAction {
        return SyncAction.valueOf(value)
    }
}