package com.arvindmaurya.taskplannerpro.components

import android.content.Context
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.Add

@Composable
fun SoundSelector(
    selectedSound: String,
    onSoundSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val soundsList = remember { getSoundsList(context) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var playingUri by remember { mutableStateOf<String?>(null) }
    var customSoundName by remember { mutableStateOf<String?>(null) }

    val filePicker = rememberFilePicker { uri ->
        val fileName = getFileNameFromUri(context, uri)
        customSoundName = fileName
        onSoundSelected(uri.toString())
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
            mediaPlayer = null
        }
    }

    AlertDialog(
        onDismissRequest = {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
            mediaPlayer = null
            onDismiss()
        },
        title = { Text("Select Alarm Sound") },
        text = {
            Column {
                // Custom sound file picker button
                Button(
                    onClick = { filePicker.launch("audio/*") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add custom sound")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Custom Sound")
                }

                if (customSoundName != null) {
                    Text(
                        text = "Selected: $customSoundName",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = "System Sounds",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                LazyColumn {
                    items(soundsList) { sound ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSoundSelected(sound.uri) }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = sound.name,
                                modifier = Modifier.weight(1f)
                            )
                            
                            IconButton(
                                onClick = {
                                    if (playingUri == sound.uri) {
                                        mediaPlayer?.apply {
                                            if (isPlaying) {
                                                stop()
                                            }
                                            release()
                                        }
                                        mediaPlayer = null
                                        playingUri = null
                                    } else {
                                        try {
                                            mediaPlayer?.apply {
                                                if (isPlaying) {
                                                    stop()
                                                }
                                                release()
                                            }
                                            mediaPlayer = MediaPlayer().apply {
                                                setDataSource(context, Uri.parse(sound.uri))
                                                setOnPreparedListener { mp ->
                                                    mp.start()
                                                    playingUri = sound.uri
                                                }
                                                setOnCompletionListener { mp ->
                                                    mp.release()
                                                    mediaPlayer = null
                                                    playingUri = null
                                                }
                                                prepareAsync()
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    if (playingUri == sound.uri) Icons.Default.StopCircle
                                    else Icons.Default.PlayCircle,
                                    contentDescription = if (playingUri == sound.uri) 
                                        "Stop Playing" else "Start Playing"
                                )
                            }

                            RadioButton(
                                selected = selectedSound == sound.uri,
                                onClick = { onSoundSelected(sound.uri) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    mediaPlayer?.apply {
                        if (isPlaying) {
                            stop()
                        }
                        release()
                    }
                    mediaPlayer = null
                    onDismiss()
                }
            ) {
                Text("Done")
            }
        }
    )
}

private data class Sound(
    val name: String,
    val uri: String
)

private fun getSoundsList(context: Context): List<Sound> {
    val manager = RingtoneManager(context)
    manager.setType(RingtoneManager.TYPE_ALARM)
    val cursor = manager.cursor

    val sounds = mutableListOf<Sound>()
    while (cursor.moveToNext()) {
        val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
        val uri = cursor.getString(RingtoneManager.URI_COLUMN_INDEX) + "/" + 
                  cursor.getString(RingtoneManager.ID_COLUMN_INDEX)
        sounds.add(Sound(title, uri))
    }
    cursor.close()
    return sounds
} 