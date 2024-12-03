package com.arvindmaurya.taskplannerpro.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.arvindmaurya.taskplannerpro.components.ErrorScreen
import com.arvindmaurya.taskplannerpro.components.FilterSheet
import com.arvindmaurya.taskplannerpro.components.LoadingScreen
import com.arvindmaurya.taskplannerpro.data.Task
import com.arvindmaurya.taskplannerpro.data.TaskFilter
import com.arvindmaurya.taskplannerpro.data.TaskPriority
import com.arvindmaurya.taskplannerpro.ui.UiState
import com.arvindmaurya.taskplannerpro.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    navController: NavController,
    viewModel: TaskViewModel,
    snackbarHostState: SnackbarHostState
) {
    val tasks by viewModel.filteredTasks.collectAsState()
    val currentFilter by viewModel.currentFilter.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var showFilterSheet by remember { mutableStateOf(false) }
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedTasks by remember { mutableStateOf(setOf<Task>()) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showSearchBar by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    if (showFilterSheet) {
        FilterSheet(
            currentFilter = currentFilter,
            onFilterChanged = { newFilter ->
                viewModel.updateFilter(newFilter)
            },
            onDismiss = { showFilterSheet = false }
        )
    }

    Scaffold(
        topBar = {
            if (showSearchBar) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { 
                        searchQuery = it
                        viewModel.updateFilter(currentFilter.copy(searchQuery = it))
                    },
                    onSearch = { showSearchBar = false },
                    active = true,
                    onActiveChange = { showSearchBar = it },
                    leadingIcon = {
                        IconButton(onClick = { showSearchBar = false }) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { 
                                searchQuery = ""
                                viewModel.updateFilter(currentFilter.copy(searchQuery = ""))
                            }) {
                                Icon(Icons.Default.Clear, "Clear")
                            }
                        }
                    }
                ) {}
            } else {
                TopAppBar(
                    title = { Text("Tasks") },
                    actions = {
                        // Selection mode actions
                        if (isSelectionMode) {
                            IconButton(onClick = { selectedTasks = emptySet(); isSelectionMode = false }) {
                                Icon(Icons.Default.Close, "Cancel Selection")
                            }
                            IconButton(
                                onClick = { showDeleteDialog = true },
                                enabled = selectedTasks.isNotEmpty()
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    "Delete Selected",
                                    tint = if (selectedTasks.isNotEmpty()) 
                                        MaterialTheme.colorScheme.error 
                                    else 
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                )
                            }
                        } else {
                            // Normal mode actions
                            IconButton(onClick = { showSearchBar = true }) {
                                Icon(Icons.Default.Search, "Search")
                            }
                            IconButton(
                                onClick = { showFilterSheet = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = "Filter",
                                    tint = if (currentFilter != TaskFilter()) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(onClick = { isSelectionMode = true }) {
                                Icon(Icons.Default.SelectAll, "Select Tasks")
                            }
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (!isSelectionMode) {
                FloatingActionButton(
                    onClick = { navController.navigate("createTask") }
                ) {
                    Icon(Icons.Default.Add, "Add Task")
                }
            }
        }
    ) { padding ->
        if (tasks.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "No tasks yet",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "Click + to add a new task",
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                items(
                    items = tasks,
                    key = { task -> task.id }
                ) { task ->
                    TaskItem(
                        task = task,
                        isSelected = task in selectedTasks,
                        isSelectionMode = isSelectionMode,
                        onTaskClick = {
                            if (isSelectionMode) {
                                selectedTasks = if (task in selectedTasks) {
                                    selectedTasks - task
                                } else {
                                    selectedTasks + task
                                }
                            } else {
                                navController.navigate("editTask/${task.id}")
                            }
                        },
                        onLongClick = {
                            if (!isSelectionMode) {
                                isSelectionMode = true
                                selectedTasks = setOf(task)
                            }
                        },
                        onTaskComplete = { viewModel.toggleTaskCompletion(task) }
                    )
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Tasks") },
            text = { Text("Are you sure you want to delete ${selectedTasks.size} selected tasks?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedTasks.forEach { task ->
                            viewModel.deleteTask(task)
                        }
                        selectedTasks = emptySet()
                        isSelectionMode = false
                        showDeleteDialog = false
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TaskItem(
    task: Task,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onTaskClick: () -> Unit,
    onLongClick: () -> Unit,
    onTaskComplete: () -> Unit
) {
    val dateTimeFormat = remember { SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault()) }
    val cardColor = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        task.isCompleted -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .combinedClickable(
                onClick = onTaskClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(
            width = 1.dp,
            color = when {
                isSelected -> MaterialTheme.colorScheme.primary
                task.isCompleted -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isSelectionMode) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { onTaskClick() },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (task.isCompleted) 
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        else 
                            MaterialTheme.colorScheme.onSurface
                    )
                }
                if (!isSelectionMode) {
                    Checkbox(
                        checked = task.isCompleted,
                        onCheckedChange = { onTaskComplete() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Time information with icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Start Time",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = dateTimeFormat.format(Date(task.startDate)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "End Time",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = dateTimeFormat.format(Date(task.endDate)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    if (task.isAlarmEnabled && task.alarmTime > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = "Alarm Time",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = dateTimeFormat.format(Date(task.alarmTime)),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }

                // Priority and Category indicators
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = when (task.priority) {
                                TaskPriority.HIGH -> MaterialTheme.colorScheme.errorContainer
                                TaskPriority.MEDIUM -> MaterialTheme.colorScheme.primaryContainer
                                TaskPriority.LOW -> MaterialTheme.colorScheme.tertiaryContainer
                            }
                        ),
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Text(
                            text = task.priority.name,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Text(
                            text = task.category.name,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
} 