package com.arvindmaurya.taskplannerpro.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY startDate ASC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Long): Task?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT * FROM tasks WHERE startDate >= :startDate AND endDate <= :endDate")
    fun getTasksInRange(startDate: Long, endDate: Long): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isCompleted = :completed")
    fun getTasksByCompletion(completed: Boolean): Flow<List<Task>>
} 