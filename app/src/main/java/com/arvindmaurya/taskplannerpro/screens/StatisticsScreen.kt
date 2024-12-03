package com.arvindmaurya.taskplannerpro.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arvindmaurya.taskplannerpro.data.TaskCategory
import com.arvindmaurya.taskplannerpro.data.TaskPriority
import com.arvindmaurya.taskplannerpro.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: TaskViewModel
) {
    val tasks by viewModel.filteredTasks.collectAsState(initial = emptyList())
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Task Statistics",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Task Counts
                val completedTasks = tasks.count { it.isCompleted }
                val totalTasks = tasks.size
                val completionRate = if (totalTasks > 0) {
                    (completedTasks.toFloat() / totalTasks) * 100
                } else 0f

                Text(
                    text = "Total Tasks: $totalTasks",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Completed Tasks: $completedTasks",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Completion Rate: ${String.format("%.1f", completionRate)}%",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Category Statistics
                Text(
                    text = "Tasks by Category",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                TaskCategory.values().forEach { category ->
                    val count = tasks.count { it.category == category }
                    if (count > 0) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = count.toString(),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Priority Statistics
                Text(
                    text = "Tasks by Priority",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                TaskPriority.values().forEach { priority ->
                    val count = tasks.count { it.priority == priority }
                    if (count > 0) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = priority.name,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = count.toString(),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
} 