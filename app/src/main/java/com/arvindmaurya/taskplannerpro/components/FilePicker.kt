package com.arvindmaurya.taskplannerpro.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

@Composable
fun rememberFilePicker(
    onFileSelected: (Uri) -> Unit
) = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
) { uri: Uri? ->
    uri?.let { onFileSelected(it) }
}

fun getFileNameFromUri(context: Context, uri: Uri): String {
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val displayNameIndex = it.getColumnIndex("_display_name")
            if (displayNameIndex != -1) {
                return it.getString(displayNameIndex)
            }
        }
    }
    return uri.lastPathSegment ?: "Unknown"
} 