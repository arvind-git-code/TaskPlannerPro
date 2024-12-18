package com.arvindmaurya.taskplannerpro.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.arvindmaurya.taskplannerpro.AlarmActivity
import com.arvindmaurya.taskplannerpro.MainActivity
import com.arvindmaurya.taskplannerpro.R
import com.arvindmaurya.taskplannerpro.data.TaskRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@HiltWorker
class TaskNotificationWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val taskRepository: TaskRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val taskId = inputData.getLong("taskId", -1)
            val isAlarm = inputData.getBoolean("isAlarm", false)
            if (taskId == -1L) return@withContext Result.failure()

            val task = taskRepository.getTaskById(taskId) ?: return@withContext Result.failure()
            
            // Calculate exact delay
            val currentTime = System.currentTimeMillis()
            val targetTime = when {
                isAlarm -> task.alarmTime
                inputData.getBoolean("isEndNotification", false) -> task.endDate
                else -> task.startDate
            }

            if (targetTime > currentTime) {
                val delay = targetTime - currentTime
                if (delay > 0) {
                    delay(delay)
                }
            }

            val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Create notification channel
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channelId = if (isAlarm) ALARM_CHANNEL_ID else NOTIFICATION_CHANNEL_ID
                val channelName = if (isAlarm) "Task Alarms" else "Task Notifications"
                val importance = if (isAlarm) 
                    NotificationManager.IMPORTANCE_HIGH 
                else 
                    NotificationManager.IMPORTANCE_DEFAULT

                val channel = NotificationChannel(channelId, channelName, importance).apply {
                    description = if (isAlarm) "Alarms for tasks" else "Notifications for tasks"
                    enableVibration(true)
                    enableLights(true)
                    if (isAlarm) {
                        setSound(
                            Uri.parse(task.alarmTone.ifEmpty { 
                                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString() 
                            }),
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_ALARM)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build()
                        )
                    }
                }
                notificationManager.createNotificationChannel(channel)
            }

            // Create intent
            val intent = Intent(appContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("taskId", taskId)
            }

            val pendingIntent = PendingIntent.getActivity(
                appContext,
                taskId.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val fullScreenIntent = Intent(appContext, AlarmActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("taskId", taskId)
                putExtra("taskTitle", task.title)
                putExtra("alarmTone", task.alarmTone)
            }

            val fullScreenPendingIntent = PendingIntent.getActivity(
                appContext,
                taskId.toInt(),
                fullScreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Build notification
            val notification = NotificationCompat.Builder(
                appContext, 
                if (isAlarm) ALARM_CHANNEL_ID else NOTIFICATION_CHANNEL_ID
            )
                .setSmallIcon(R.drawable.ic_task_notification)
                .setContentTitle(task.title)
                .setContentText(
                    when {
                        isAlarm -> "Alarm for task"
                        System.currentTimeMillis() >= task.endDate -> "Task ended"
                        else -> "Task started"
                    }
                )
                .setPriority(if (isAlarm) NotificationCompat.PRIORITY_MAX else NotificationCompat.PRIORITY_HIGH)
                .setCategory(if (isAlarm) NotificationCompat.CATEGORY_ALARM else NotificationCompat.CATEGORY_EVENT)
                .setAutoCancel(true)
                .setFullScreenIntent(if (isAlarm) fullScreenPendingIntent else null, true)
                .setContentIntent(pendingIntent)
                .apply {
                    if (isAlarm) {
                        setSound(
                            Uri.parse(task.alarmTone.ifEmpty { 
                                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString() 
                            }),
                            AudioManager.STREAM_ALARM
                        )
                        setVibrate(longArrayOf(0, 1000, 500, 1000))
                    }
                }
                .build()

            notificationManager.notify(
                if (isAlarm) taskId.toInt() else taskId.toInt() + 1000,
                notification
            )

            if (isAlarm) {
                // Launch AlarmActivity instead of playing sound directly
                val alarmIntent = Intent(appContext, AlarmActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    putExtra("taskId", taskId)
                    putExtra("taskTitle", task.title)
                    putExtra("alarmTone", task.alarmTone)
                }
                appContext.startActivity(alarmIntent)
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "task_notifications"
        private const val ALARM_CHANNEL_ID = "task_alarms"
    }
} 