package com.arvindmaurya.taskplannerpro

import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arvindmaurya.taskplannerpro.ui.theme.TaskPlannerProTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlarmActivity : ComponentActivity() {
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val taskId = intent.getLongExtra("taskId", -1)
        val taskTitle = intent.getStringExtra("taskTitle") ?: "Task Alarm"
        val alarmTone = intent.getStringExtra("alarmTone") ?: ""

        // Start playing alarm
        startAlarm(alarmTone)

        setContent {
            TaskPlannerProTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = taskTitle,
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = { 
                                stopAlarm()
                                finish()
                            },
                            modifier = Modifier.size(width = 200.dp, height = 64.dp)
                        ) {
                            Text("Stop Alarm")
                        }
                    }
                }
            }
        }
    }

    private fun startAlarm(alarmTone: String) {
        mediaPlayer = MediaPlayer().apply {
            val uri = if (alarmTone.isNotEmpty()) {
                Uri.parse(alarmTone)
            } else {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            }
            setDataSource(this@AlarmActivity, uri)
            isLooping = true
            prepare()
            start()
        }
    }

    private fun stopAlarm() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlarm()
    }
} 