package com.example.taskflow.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taskflow.data.Priority
import com.example.taskflow.data.Task
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompletedTasksScreen(
    viewModel: TaskViewModel,
    onBackClick: () -> Unit
) {
    val allTasks by viewModel.allTasks.collectAsState()
    val completedTasks = remember(allTasks) {
        allTasks.filter { it.isCompleted }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Completed Tasks",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (completedTasks.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            completedTasks.forEach { viewModel.deleteTask(it) }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFB71C1C)
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFB71C1C))
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = null,
                            tint = Color(0xFFB71C1C)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Clear All Completed",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB71C1C)
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Summary Banner
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF3E8FF) // Purple tint matching mockup
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF6B21A8)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "${completedTasks.size} tasks completed",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF581C87),
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Keep it up! You're in the top 5% this week.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF6B21A8)
                            )
                        }
                    }
                }
            }

            // Completed Task Items List
            if (completedTasks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No completed tasks yet.",
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                items(completedTasks, key = { it.id }) { task ->
                    CompletedTaskCard(
                        task = task,
                        onRestore = {
                            viewModel.updateTask(task.copy(isCompleted = false))
                        },
                        onDelete = {
                            viewModel.deleteTask(task)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CompletedTaskCard(
    task: Task,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("MMM dd", Locale.getDefault()) }
    val formattedDate = remember(task.dueDate) { dateFormatter.format(Date(task.dueDate)) }

    val (badgeBg, badgeText) = when (task.priority) {
        Priority.HIGH -> Color(0xFFFFEBEE) to Color(0xFFD32F2F)
        Priority.MEDIUM -> Color(0xFFFFF3E0) to Color(0xFFE65100)
        Priority.LOW -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: Priority Badge & Due Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeBg
                ) {
                    Text(
                        text = task.priority.name.lowercase().replaceFirstChar { it.uppercase() },
                        color = badgeText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formattedDate,
                        fontSize = 12.sp,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Title & Description
            Text(
                text = task.title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Gray,
                textDecoration = TextDecoration.LineThrough
            )

            if (task.description.isNotBlank()) {
                Text(
                    text = task.description,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    maxLines = 2
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = Color(0xFFEEEEEE)
            )

            // Actions Footer: Completed tag + Restore & Delete buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Completed",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Restore Button
                    IconButton(
                        onClick = onRestore,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFF3E8FF), RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "Restore Task",
                            tint = Color(0xFF6B21A8),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Delete Button
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFFFEBEE), RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete Task",
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}