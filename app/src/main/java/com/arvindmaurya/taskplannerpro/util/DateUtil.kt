package com.arvindmaurya.taskplannerpro.util

import com.arvindmaurya.taskplannerpro.data.RepeatInterval
import java.util.*

object DateUtil {
    fun getNextOccurrence(currentDate: Long, repeatInterval: RepeatInterval): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = currentDate
        }
        
        when (repeatInterval) {
            RepeatInterval.DAILY -> calendar.add(Calendar.DAY_OF_YEAR, 1)
            RepeatInterval.WEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            RepeatInterval.MONTHLY -> calendar.add(Calendar.MONTH, 1)
            RepeatInterval.YEARLY -> calendar.add(Calendar.YEAR, 1)
            RepeatInterval.NONE -> return currentDate
        }
        
        return calendar.timeInMillis
    }

    fun formatDate(date: Long): String {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = date
        }
        return String.format(
            "%02d/%02d/%d",
            calendar.get(Calendar.DAY_OF_MONTH),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.YEAR)
        )
    }
} 