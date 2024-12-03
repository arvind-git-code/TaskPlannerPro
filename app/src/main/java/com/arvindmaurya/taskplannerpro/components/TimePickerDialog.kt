package com.arvindmaurya.taskplannerpro.components

import android.app.TimePickerDialog
import android.text.format.DateFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AlarmTimePicker(
    alarmTime: Long,
    onTimeSelected: (Long) -> Unit,
    enabled: Boolean = true
) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    if (alarmTime > 0) {
        calendar.timeInMillis = alarmTime
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(enabled = enabled) {
                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                        calendar.set(Calendar.MINUTE, minute)
                        onTimeSelected(calendar.timeInMillis)
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    DateFormat.is24HourFormat(context)
                ).show()
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Alarm Time")
        Text(
            text = if (alarmTime > 0) timeFormat.format(Date(alarmTime)) else "Set Time",
            style = MaterialTheme.typography.bodyLarge,
            color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
    }
} 