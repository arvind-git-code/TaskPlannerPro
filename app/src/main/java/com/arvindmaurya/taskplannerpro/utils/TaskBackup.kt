package com.arvindmaurya.taskplannerpro.utils

import android.content.Context
import com.arvindmaurya.taskplannerpro.data.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.io.File
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object TaskBackup {
    private val json = Json { 
        prettyPrint = true
        ignoreUnknownKeys = true 
    }

    suspend fun exportTasks(context: Context, tasks: List<Task>) {
        withContext(Dispatchers.IO) {
            try {
                val jsonString = json.encodeToString(tasks)
                // Use encrypted shared preferences
                val masterKey = MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()

                val encryptedPrefs = EncryptedSharedPreferences.create(
                    context,
                    "backup_prefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
                encryptedPrefs.edit().putString("tasks_backup", jsonString).apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun importTasks(context: Context): List<Task>? {
        return withContext(Dispatchers.IO) {
            try {
                val masterKey = MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()

                val encryptedPrefs = EncryptedSharedPreferences.create(
                    context,
                    "backup_prefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )

                val jsonString = encryptedPrefs.getString("tasks_backup", null)
                jsonString?.let { json.decodeFromString<List<Task>>(it) }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
} 