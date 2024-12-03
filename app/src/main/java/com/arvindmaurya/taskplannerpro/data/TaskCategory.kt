package com.arvindmaurya.taskplannerpro.data

import kotlinx.serialization.Serializable

@Serializable
enum class TaskCategory {
    PERSONAL,
    WORK,
    SHOPPING,
    HEALTH,
    FINANCE,
    LEARNING,
    TRAVEL,
    HOME,
    SOCIAL,
    ENTERTAINMENT,
    OTHER
} 