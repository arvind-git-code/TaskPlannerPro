package com.arvindmaurya.taskplannerpro.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.arvindmaurya.taskplannerpro.data.RepeatInterval
import com.arvindmaurya.taskplannerpro.data.Task
import com.arvindmaurya.taskplannerpro.viewmodel.TaskViewModel
import androidx.navigation.NavController
import com.arvindmaurya.taskplannerpro.components.AlarmTimePicker
import com.arvindmaurya.taskplannerpro.components.SoundSelector
import com.arvindmaurya.taskplannerpro.data.TaskCategory
import com.arvindmaurya.taskplannerpro.data.TaskPriority
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskScreen(
    taskId: Long,
    navController: NavController,
    viewModel: TaskViewModel
) {
    var task by remember { mutableStateOf<Task?>(null) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf(Calendar.getInstance()) }
    var endDate by remember { mutableStateOf(Calendar.getInstance()) }
    var repeatInterval by remember { mutableStateOf(RepeatInterval.NONE) }
    var isAlarmEnabled by remember { mutableStateOf(false) }
    var selectedSound by remember { mutableStateOf("") }
    var alarmTime by remember { mutableStateOf(0L) }
    var showSoundSelector by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var expandedDropdown by remember { mutableStateOf(false) }
    var priority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    var category by remember { mutableStateOf(TaskCategory.OTHER) }
    
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    // Load task data
    LaunchedEffect(taskId) {
        viewModel.getTaskById(taskId)?.let { existingTask ->
            task = existingTask
            title = existingTask.title
            description = existingTask.description
            startDate.timeInMillis = existingTask.startDate
            endDate.timeInMillis = existingTask.endDate
            repeatInterval = existingTask.repeatInterval
            isAlarmEnabled = existingTask.isAlarmEnabled
            selectedSound = existingTask.alarmTone
            alarmTime = existingTask.alarmTime
            priority = existingTask.priority
            category = existingTask.category
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Task") },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Task")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                // Start Date and Time
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = {
                            DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    startDate.set(year, month, day)
                                },
                                startDate.get(Calendar.YEAR),
                                startDate.get(Calendar.MONTH),
                                startDate.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Start: ${dateFormat.format(startDate.time)}")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    startDate.set(Calendar.HOUR_OF_DAY, hour)
                                    startDate.set(Calendar.MINUTE, minute)
                                },
                                startDate.get(Calendar.HOUR_OF_DAY),
                                startDate.get(Calendar.MINUTE),
                                false
                            ).show()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(timeFormat.format(startDate.time))
                    }
                }

                // End Date and Time
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = {
                            DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    endDate.set(year, month, day)
                                },
                                endDate.get(Calendar.YEAR),
                                endDate.get(Calendar.MONTH),
                                endDate.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("End: ${dateFormat.format(endDate.time)}")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    endDate.set(Calendar.HOUR_OF_DAY, hour)
                                    endDate.set(Calendar.MINUTE, minute)
                                },
                                endDate.get(Calendar.HOUR_OF_DAY),
                                endDate.get(Calendar.MINUTE),
                                false
                            ).show()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(timeFormat.format(endDate.time))
                    }
                }

                // Repeat Interval Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedDropdown,
                    onExpandedChange = { expandedDropdown = !expandedDropdown },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    TextField(
                        value = repeatInterval.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Repeat Interval") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        RepeatInterval.values().forEach { interval ->
                            DropdownMenuItem(
                                text = { Text(interval.name) },
                                onClick = {
                                    repeatInterval = interval
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }

                // Alarm Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable Alarm")
                    Switch(
                        checked = isAlarmEnabled,
                        onCheckedChange = { isAlarmEnabled = it }
                    )
                }

                // After Alarm Switch
                if (isAlarmEnabled) {
                    AlarmTimePicker(
                        alarmTime = alarmTime,
                        onTimeSelected = { alarmTime = it }
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { showSoundSelector = true },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Alarm Sound")
                        Text(
                            text = if (selectedSound.isNotEmpty()) "Selected" else "Choose Sound",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Priority Selection
                Text(
                    text = "Priority",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TaskPriority.values().forEach { priorityOption ->
                        FilterChip(
                            selected = priority == priorityOption,
                            onClick = { priority = priorityOption },
                            label = { Text(priorityOption.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = when (priorityOption) {
                                    TaskPriority.HIGH -> MaterialTheme.colorScheme.errorContainer
                                    TaskPriority.MEDIUM -> MaterialTheme.colorScheme.primaryContainer
                                    TaskPriority.LOW -> MaterialTheme.colorScheme.tertiaryContainer
                                }
                            )
                        )
                    }
                }

                // Category Selection
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(TaskCategory.values().toList()) { categoryOption ->
                        FilterChip(
                            selected = category == categoryOption,
                            onClick = { category = categoryOption },
                            label = { Text(categoryOption.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Save Button
                Button(
                    onClick = {
                        task?.let { existingTask ->
                            viewModel.updateTask(
                                existingTask.copy(
                                    title = title,
                                    description = description,
                                    startDate = startDate.timeInMillis,
                                    endDate = endDate.timeInMillis,
                                    startTime = startDate.timeInMillis,
                                    endTime = endDate.timeInMillis,
                                    repeatInterval = repeatInterval,
                                    alarmTone = selectedSound,
                                    alarmTime = alarmTime,
                                    isAlarmEnabled = isAlarmEnabled,
                                    priority = priority,
                                    category = category
                                )
                            )
                        }
                        navController.navigateUp()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Text("Save Changes")
                }
            }
        }
    }

    if (showSoundSelector) {
        SoundSelector(
            selectedSound = selectedSound,
            onSoundSelected = { selectedSound = it },
            onDismiss = { showSoundSelector = false }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Task") },
            text = { Text("Are you sure you want to delete this task?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        task?.let { viewModel.deleteTask(it) }
                        showDeleteDialog = false
                        navController.navigateUp()
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
} 