package com.arvindmaurya.taskplannerpro.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arvindmaurya.taskplannerpro.data.SortOrder
import com.arvindmaurya.taskplannerpro.data.TaskCategory
import com.arvindmaurya.taskplannerpro.data.TaskFilter
import com.arvindmaurya.taskplannerpro.data.TaskPriority

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSheet(
    currentFilter: TaskFilter,
    onFilterChanged: (TaskFilter) -> Unit,
    onDismiss: () -> Unit
) {
    var tempFilter by remember { mutableStateOf(currentFilter) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Filter & Sort",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Show completed switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Show Completed Tasks")
                Switch(
                    checked = tempFilter.showCompleted,
                    onCheckedChange = { 
                        tempFilter = tempFilter.copy(showCompleted = it)
                    }
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Sort order
            Text(
                text = "Sort By",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            Column {
                SortOrder.values().forEach { sortOrder ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                tempFilter = tempFilter.copy(sortOrder = sortOrder)
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = tempFilter.sortOrder == sortOrder,
                            onClick = { 
                                tempFilter = tempFilter.copy(sortOrder = sortOrder)
                            }
                        )
                        Text(
                            text = when (sortOrder) {
                                SortOrder.DATE_ASC -> "Date (Oldest First)"
                                SortOrder.DATE_DESC -> "Date (Newest First)"
                                SortOrder.TITLE_ASC -> "Title (A-Z)"
                                SortOrder.TITLE_DESC -> "Title (Z-A)"
                            },
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Priority Filter
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
                TaskPriority.values().forEach { priority ->
                    FilterChip(
                        selected = priority in tempFilter.selectedPriorities,
                        onClick = {
                            val newPriorities = tempFilter.selectedPriorities.toMutableSet()
                            if (priority in newPriorities) {
                                newPriorities.remove(priority)
                            } else {
                                newPriorities.add(priority)
                            }
                            tempFilter = tempFilter.copy(selectedPriorities = newPriorities)
                        },
                        label = { Text(priority.name) }
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Category Filter
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
                items(TaskCategory.values().toList()) { category ->
                    FilterChip(
                        selected = category in tempFilter.selectedCategories,
                        onClick = {
                            val newCategories = tempFilter.selectedCategories.toMutableSet()
                            if (category in newCategories) {
                                newCategories.remove(category)
                            } else {
                                newCategories.add(category)
                            }
                            tempFilter = tempFilter.copy(selectedCategories = newCategories)
                        },
                        label = { Text(category.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Buttons Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Reset Button
                OutlinedButton(
                    onClick = {
                        tempFilter = TaskFilter()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reset")
                }

                // Apply Button
                Button(
                    onClick = {
                        onFilterChanged(tempFilter)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Apply")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
} 