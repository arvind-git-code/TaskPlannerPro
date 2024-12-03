package com.arvindmaurya.taskplannerpro.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "startDate")
    val startDate: Long,

    @ColumnInfo(name = "endDate")
    val endDate: Long,

    @ColumnInfo(name = "startTime")
    val startTime: Long,

    @ColumnInfo(name = "endTime")
    val endTime: Long,

    @ColumnInfo(name = "alarmTime")
    val alarmTime: Long = 0,

    @ColumnInfo(name = "repeatInterval")
    val repeatInterval: RepeatInterval,

    @ColumnInfo(name = "alarmTone")
    val alarmTone: String,

    @ColumnInfo(name = "isAlarmEnabled")
    val isAlarmEnabled: Boolean,

    @ColumnInfo(name = "isCompleted")
    val isCompleted: Boolean = false,

    @ColumnInfo(name = "category")
    val category: TaskCategory = TaskCategory.OTHER,

    @ColumnInfo(name = "priority")
    val priority: TaskPriority = TaskPriority.MEDIUM
)

@Serializable
enum class RepeatInterval {
    NONE,
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}