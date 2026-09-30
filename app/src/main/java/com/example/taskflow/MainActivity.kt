package com.example.taskflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.taskflow.ui.CompletedTasksScreen
import com.example.taskflow.ui.TaskAddEditScreen
import com.example.taskflow.ui.TaskListScreen
import com.example.taskflow.ui.TaskViewModel
import com.example.taskflow.ui.theme.TaskFlowTheme

class MainActivity : ComponentActivity() {

    private val viewModel: TaskViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TaskFlowTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TaskFlowNavigation(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun TaskFlowNavigation(viewModel: TaskViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "task_list"
    ) {
        composable("task_list") {
            TaskListScreen(
                viewModel = viewModel,
                onAddTaskClick = {
                    navController.navigate("add_edit_task")
                },
                onTaskClick = { taskId ->
                    navController.navigate("add_edit_task?taskId=$taskId")
                },
                onCompletedTasksClick = {
                    navController.navigate("completed_tasks")
                }
            )
        }

        composable(
            route = "add_edit_task?taskId={taskId}",
            arguments = listOf(
                navArgument("taskId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val taskIdStr = backStackEntry.arguments?.getString("taskId")
            val taskId = taskIdStr?.toLongOrNull()

            TaskAddEditScreen(
                taskId = taskId,
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable("completed_tasks") {
            CompletedTasksScreen(
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}