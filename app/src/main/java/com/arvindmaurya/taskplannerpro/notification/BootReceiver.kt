package com.arvindmaurya.taskplannerpro.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.*
import com.arvindmaurya.taskplannerpro.data.TaskRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    @Inject
    lateinit var taskRepository: TaskRepository

    @Inject
    lateinit var workManager: WorkManager

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            CoroutineScope(Dispatchers.IO).launch {
                // Reschedule all tasks
                taskRepository.allTasks.collect { tasks ->
                    tasks.forEach { task ->
                        if (task.isAlarmEnabled && !task.isCompleted) {
                            // Reschedule notification
                            val notificationWork = OneTimeWorkRequestBuilder<TaskNotificationWorker>()
                                .setInputData(
                                    Data.Builder()
                                        .putLong("taskId", task.id)
                                        .build()
                                )
                                .setInitialDelay(
                                    task.startDate - System.currentTimeMillis(),
                                    TimeUnit.MILLISECONDS
                                )
                                .build()

                            workManager.enqueueUniqueWork(
                                "task_notification_${task.id}",
                                ExistingWorkPolicy.REPLACE,
                                notificationWork
                            )
                        }
                    }
                }
            }
        }
    }
} 