package com.arvindmaurya.taskplannerpro.data

import androidx.room.TypeConverter

class Converters {
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