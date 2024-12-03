package com.arvindmaurya.taskplannerpro.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.arvindmaurya.taskplannerpro.data.RepeatInterval
import com.arvindmaurya.taskplannerpro.data.Task
import com.arvindmaurya.taskplannerpro.data.TaskRepository
import com.arvindmaurya.taskplannerpro.notification.TaskNotificationWorker
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.*
import java.util.concurrent.TimeUnit

@HiltWorker
class RepeatTaskWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val taskRepository: TaskRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val taskId = inputData.getLong("taskId", -1)
        if (taskId == -1L) return Result.failure()

        val task = taskRepository.getTaskById(taskId) ?: return Result.failure()
        
        // Calculate next occurrence based on repeat interval
        val calendar = Calendar.getInstance().apply {
            timeInMillis = task.startDate
        }
        
        when (task.repeatInterval) {
            RepeatInterval.DAILY -> calendar.add(Calendar.DAY_OF_YEAR, 1)
            RepeatInterval.WEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            RepeatInterval.MONTHLY -> calendar.add(Calendar.MONTH, 1)
            RepeatInterval.YEARLY -> calendar.add(Calendar.YEAR, 1)
            RepeatInterval.NONE -> return Result.success()
        }

        // Create new task for next occurrence
        val newTask = task.copy(
            id = 0,
            startDate = calendar.timeInMillis,
            endDate = calendar.timeInMillis + (task.endDate - task.startDate),
            isCompleted = false
        )
        
        val newTaskId = taskRepository.insertTask(newTask)

        // Schedule notification for new task
        scheduleNotification(newTaskId, newTask)
        
        // Schedule next repetition
        scheduleNextRepetition(newTaskId, newTask)

        return Result.success()
    }

    private fun scheduleNotification(taskId: Long, task: Task) {
        val notificationWork = OneTimeWorkRequestBuilder<TaskNotificationWorker>()
            .setInputData(workDataOf("taskId" to taskId))
            .setInitialDelay(
                task.startDate - System.currentTimeMillis(),
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(applicationContext)
            .enqueueUniqueWork(
                "task_notification_$taskId",
                ExistingWorkPolicy.REPLACE,
                notificationWork
            )
    }

    private fun scheduleNextRepetition(taskId: Long, task: Task) {
        if (task.repeatInterval == RepeatInterval.NONE) return

        val repeatWork = OneTimeWorkRequestBuilder<RepeatTaskWorker>()
            .setInputData(workDataOf("taskId" to taskId))
            .setInitialDelay(
                when (task.repeatInterval) {
                    RepeatInterval.DAILY -> 24L
                    RepeatInterval.WEEKLY -> 7 * 24L
                    RepeatInterval.MONTHLY -> 30 * 24L
                    RepeatInterval.YEARLY -> 365 * 24L
                    RepeatInterval.NONE -> return
                },
                TimeUnit.HOURS
            )
            .build()

        WorkManager.getInstance(applicationContext)
            .enqueueUniqueWork(
                "task_repeat_$taskId",
                ExistingWorkPolicy.REPLACE,
                repeatWork
            )
    }
} 