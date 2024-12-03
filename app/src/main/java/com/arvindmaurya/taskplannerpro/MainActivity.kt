package com.arvindmaurya.taskplannerpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import com.arvindmaurya.taskplannerpro.ui.theme.TaskPlannerProTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.tooling.preview.Preview
import com.arvindmaurya.taskplannerpro.screens.TaskListScreen
import com.arvindmaurya.taskplannerpro.screens.CreateTaskScreen
import com.arvindmaurya.taskplannerpro.screens.EditTaskScreen
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel
import android.Manifest
import android.os.Build
import androidx.compose.runtime.remember
import androidx.work.Configuration
import androidx.work.WorkManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.PowerManager
import com.arvindmaurya.taskplannerpro.service.TaskPlannerService
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        private const val PERMISSION_REQUEST_CODE = 123
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Request permissions
        checkAndRequestPermissions()
        
        // Start foreground service safely
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(Intent(this, TaskPlannerService::class.java))
            } else {
                startService(Intent(this, TaskPlannerService::class.java))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Request battery optimization exemption
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(POWER_SERVICE) as PowerManager
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                try {
                    val intent = Intent().apply {
                        action = android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        
        enableEdgeToEdge()
        setContent {
            TaskPlannerProTheme {
                TaskPlannerApp()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Don't stop the service when activity is destroyed
        // This ensures background operation continues
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val requiredPermissions = arrayOf(
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.SCHEDULE_EXACT_ALARM,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.VIBRATE,
                Manifest.permission.WAKE_LOCK,
                Manifest.permission.FOREGROUND_SERVICE
            )

            val permissionsToRequest = requiredPermissions.filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }

            if (permissionsToRequest.isNotEmpty()) {
                ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toTypedArray(),
                    PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    // Add permission result handling
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && 
                    grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    // All permissions granted, proceed with initialization
                } else {
                    // Handle permission denial
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskPlannerApp() {
    val navController = rememberNavController()
    val currentBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry.value?.destination?.route
    val snackbarHostState = remember { SnackbarHostState() }
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = when (currentRoute) {
                            "taskList" -> "Task List"
                            "createTask" -> "Create Task"
                            else -> if (currentRoute?.startsWith("editTask") == true) "Edit Task" else ""
                        }
                    )
                },
                navigationIcon = {
                    if (currentRoute != "taskList") {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "taskList",
            modifier = Modifier.padding(padding)
        ) {
            composable("taskList") {
                TaskListScreen(
                    navController = navController,
                    viewModel = hiltViewModel(),
                    snackbarHostState = snackbarHostState
                )
            }
            composable("createTask") {
                CreateTaskScreen(
                    navController = navController,
                    viewModel = hiltViewModel()
                )
            }
            composable("editTask/{taskId}") { backStackEntry ->
                EditTaskScreen(
                    taskId = backStackEntry.arguments?.getString("taskId")?.toLongOrNull() ?: 0,
                    navController = navController,
                    viewModel = hiltViewModel()
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TaskPlannerAppPreview() {
    TaskPlannerProTheme {
        TaskPlannerApp()
    }
}