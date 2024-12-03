package com.arvindmaurya.taskplannerpro.data

enum class SortOrder {
    DATE_ASC,
    DATE_DESC,
    TITLE_ASC,
    TITLE_DESC
}

data class TaskFilter(
    val showCompleted: Boolean = true,
    val sortOrder: SortOrder = SortOrder.DATE_ASC,
    val dateRange: Pair<Long, Long>? = null,
    val searchQuery: String = "",
    val selectedCategories: Set<TaskCategory> = emptySet(),
    val selectedPriorities: Set<TaskPriority> = emptySet()
) 