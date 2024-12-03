package com.arvindmaurya.taskplannerpro.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.arvindmaurya.taskplannerpro.data.RepeatInterval
import com.arvindmaurya.taskplannerpro.data.SortOrder
import com.arvindmaurya.taskplannerpro.data.Task
import com.arvindmaurya.taskplannerpro.data.TaskCategory
import com.arvindmaurya.taskplannerpro.data.TaskFilter
import com.arvindmaurya.taskplannerpro.data.TaskPriority
import com.arvindmaurya.taskplannerpro.data.TaskRepository
import com.arvindmaurya.taskplannerpro.notification.TaskNotificationWorker
import com.arvindmaurya.taskplannerpro.worker.RepeatTaskWorker
import com.arvindmaurya.taskplannerpro.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val workManager: WorkManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    private val _currentFilter = MutableStateFlow(TaskFilter())
    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Success(Unit))

    val uiState: StateFlow<UiState<Unit>> = _uiState.asStateFlow()
    val currentFilter: StateFlow<TaskFilter> = _currentFilter.asStateFlow()

    val filteredTasks = combine(_tasks, _currentFilter) { tasks, filter ->
        tasks.filter { task ->
            val matchesCompletion = filter.showCompleted || !task.isCompleted
            
            val matchesSearch = filter.searchQuery.isEmpty() || 
                task.title.contains(filter.searchQuery, ignoreCase = true) ||
                task.description.contains(filter.searchQuery, ignoreCase = true)
            
            val matchesDateRange = filter.dateRange?.let { (start, end) ->
                task.startDate >= start && task.endDate <= end
            } ?: true

            val matchesCategory = filter.selectedCategories.isEmpty() || 
                task.category in filter.selectedCategories

            val matchesPriority = filter.selectedPriorities.isEmpty() || 
                task.priority in filter.selectedPriorities
            
            matchesCompletion && matchesSearch && matchesDateRange && 
                matchesCategory && matchesPriority
        }.let { filteredTasks ->
            when (filter.sortOrder) {
                SortOrder.DATE_ASC -> filteredTasks.sortedBy { it.startDate }
                SortOrder.DATE_DESC -> filteredTasks.sortedByDescending { it.startDate }
                SortOrder.TITLE_ASC -> filteredTasks.sortedBy { it.title }
                SortOrder.TITLE_DESC -> filteredTasks.sortedByDescending { it.title }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        loadTasks()
    }

    private fun loadTasks() {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                repository.allTasks.collect { taskList ->
                    _tasks.value = taskList
                    _uiState.value = UiState.Success(Unit)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun addTask(
        title: String,
        description: String,
        startDate: Long,
        endDate: Long,
        startTime: Long,
        endTime: Long,
        repeatInterval: RepeatInterval,
        alarmTone: String,
        alarmTime: Long,
        isAlarmEnabled: Boolean,
        priority: TaskPriority,
        category: TaskCategory
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                val task = Task(
                    title = title,
                    description = description,
                    startDate = startDate,
                    endDate = endDate,
                    startTime = startTime,
                    endTime = endTime,
                    repeatInterval = repeatInterval,
                    alarmTone = alarmTone,
                    alarmTime = alarmTime,
                    isAlarmEnabled = isAlarmEnabled,
                    priority = priority,
                    category = category
                )
                val taskId = repository.insertTask(task)
                val newTask = task.copy(id = taskId)
                scheduleTaskNotification(newTask)
                _uiState.value = UiState.Success(Unit)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to add task")
            }
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                repository.updateTask(task)
                scheduleTaskNotification(task)
                _uiState.value = UiState.Success(Unit)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to update task")
            }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                repository.deleteTask(task)
                workManager.cancelUniqueWork("task_notification_${task.id}")
                _uiState.value = UiState.Success(Unit)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to delete task")
            }
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    suspend fun getTaskById(id: Long): Task? {
        return repository.getTaskById(id)
    }

    private fun scheduleTaskNotification(task: Task) {
        // Schedule start notification
        val startNotificationWork = OneTimeWorkRequestBuilder<TaskNotificationWorker>()
            .setInputData(
                workDataOf(
                    "taskId" to task.id,
                    "isAlarm" to false
                )
            )
            .setInitialDelay(
                task.startDate - System.currentTimeMillis(),
                TimeUnit.MILLISECONDS
            )
            .build()

        workManager.enqueueUniqueWork(
            "task_start_notification_${task.id}",
            ExistingWorkPolicy.REPLACE,
            startNotificationWork
        )

        // Schedule end notification
        val endNotificationWork = OneTimeWorkRequestBuilder<TaskNotificationWorker>()
            .setInputData(
                workDataOf(
                    "taskId" to task.id,
                    "isAlarm" to false
                )
            )
            .setInitialDelay(
                task.endDate - System.currentTimeMillis(),
                TimeUnit.MILLISECONDS
            )
            .build()

        workManager.enqueueUniqueWork(
            "task_end_notification_${task.id}",
            ExistingWorkPolicy.REPLACE,
            endNotificationWork
        )

        // Schedule alarm if enabled
        if (task.isAlarmEnabled && task.alarmTime > 0) {
            val alarmWork = OneTimeWorkRequestBuilder<TaskNotificationWorker>()
                .setInputData(
                    workDataOf(
                        "taskId" to task.id,
                        "isAlarm" to true
                    )
                )
                .setInitialDelay(
                    task.alarmTime - System.currentTimeMillis(),
                    TimeUnit.MILLISECONDS
                )
                .build()

            workManager.enqueueUniqueWork(
                "task_alarm_${task.id}",
                ExistingWorkPolicy.REPLACE,
                alarmWork
            )
        }
    }

    fun updateFilter(filter: TaskFilter) {
        _currentFilter.value = filter
    }
} 