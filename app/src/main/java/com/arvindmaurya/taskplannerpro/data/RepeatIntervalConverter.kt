package com.arvindmaurya.taskplannerpro.data

import androidx.room.TypeConverter

class RepeatIntervalConverter {
    @TypeConverter
    fun fromRepeatInterval(value: RepeatInterval): String {
        return value.name
    }

    @TypeConverter
    fun toRepeatInterval(value: String): RepeatInterval {
        return try {
            RepeatInterval.valueOf(value)
        } catch (e: IllegalArgumentException) {
            RepeatInterval.NONE
        }
    }
} 