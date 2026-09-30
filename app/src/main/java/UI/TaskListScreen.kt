package com.example.taskflow.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taskflow.data.Priority
import com.example.taskflow.data.Task
import java.text.SimpleDateFormat
import java.util.*

enum class TaskSortOption(val label: String) {
    PRIORITY_HIGHEST("Priority (Highest First)"),
    DUE_DATE_SOONEST("Due Date (Soonest First)"),
    TITLE_AZ("Task Title (A-Z)")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    viewModel: TaskViewModel,
    onAddTaskClick: () -> Unit,
    onTaskClick: (Long) -> Unit,
    onCompletedTasksClick: () -> Unit
) {
    val tasks by viewModel.allTasks.collectAsState()

    var selectedPriorityFilter by remember { mutableStateOf<Priority?>(Priority.HIGH) }
    var currentSortOption by remember { mutableStateOf(TaskSortOption.PRIORITY_HIGHEST) }
    var showBottomSheet by remember { mutableStateOf(false) }

    val activeTasks = remember(tasks) { tasks.filter { !it.isCompleted } }

    val filteredAndSortedTasks = remember(activeTasks, selectedPriorityFilter, currentSortOption) {
        activeTasks
            .filter { task ->
                selectedPriorityFilter == null || task.priority == selectedPriorityFilter
            }
            .sortedWith { t1, t2 ->
                when (currentSortOption) {
                    TaskSortOption.PRIORITY_HIGHEST -> t2.priority.ordinal.compareTo(t1.priority.ordinal)
                    TaskSortOption.DUE_DATE_SOONEST -> t1.dueDate.compareTo(t2.dueDate)
                    TaskSortOption.TITLE_AZ -> t1.title.compareTo(t2.title, ignoreCase = true)
                }
            }
    }

    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "TaskFlow",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1B1F)
                    )
                },
                actions = {
                    IconButton(onClick = { /* Search Action */ }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF1C1B1F)
                        )
                    }
                    IconButton(onClick = onCompletedTasksClick) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Options",
                            tint = Color(0xFF1C1B1F)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTaskClick,
                containerColor = Color(0xFF6750A4),
                contentColor = Color.White
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Task")
            }
        },
        containerColor = Color(0xFFF6F6F8)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filter Pills Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    FilterPill(
                        text = "All Tasks",
                        isSelected = selectedPriorityFilter == null,
                        onClick = { selectedPriorityFilter = null }
                    )
                }
                item {
                    FilterPill(
                        text = "High Priority",
                        isSelected = selectedPriorityFilter == Priority.HIGH,
                        hasDot = true,
                        dotColor = Color(0xFFD32F2F),
                        onClick = {
                            selectedPriorityFilter = if (selectedPriorityFilter == Priority.HIGH) null else Priority.HIGH
                        }
                    )
                }
                item {
                    FilterPill(
                        text = "Medium Priority",
                        isSelected = selectedPriorityFilter == Priority.MEDIUM,
                        hasDot = true,
                        dotColor = Color(0xFFF57C00),
                        onClick = {
                            selectedPriorityFilter = if (selectedPriorityFilter == Priority.MEDIUM) null else Priority.MEDIUM
                        }
                    )
                }
                item {
                    FilterPill(
                        text = "Low Priority",
                        isSelected = selectedPriorityFilter == Priority.LOW,
                        hasDot = true,
                        dotColor = Color(0xFF388E3C),
                        onClick = {
                            selectedPriorityFilter = if (selectedPriorityFilter == Priority.LOW) null else Priority.LOW
                        }
                    )
                }
            }

            // Task Summary Count & Sort Trigger
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Showing ${filteredAndSortedTasks.size} of ${activeTasks.size} tasks",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Row(
                    modifier = Modifier.clickable { showBottomSheet = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Priority",
                        color = Color(0xFF673AB7),
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Sort Options",
                        tint = Color(0xFF673AB7)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Task List
            if (filteredAndSortedTasks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tasks available.",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredAndSortedTasks, key = { it.id }) { task ->
                        TaskCardItem(
                            task = task,
                            onClick = { onTaskClick(task.id) }
                        )
                    }
                }
            }
        }

        // Sort Bottom Sheet Modal
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Sort Tasks By",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1B1F)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Select one option below to arrange your layout",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    TaskSortOption.entries.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    currentSortOption = option
                                    showBottomSheet = false
                                }
                                .padding(vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = option.label,
                                fontSize = 16.sp,
                                fontWeight = if (currentSortOption == option) FontWeight.Bold else FontWeight.Medium,
                                color = if (currentSortOption == option) Color(0xFF673AB7) else Color(0xFF1C1B1F)
                            )

                            if (currentSortOption == option) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color(0xFF673AB7)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun FilterPill(
    text: String,
    isSelected: Boolean,
    hasDot: Boolean = false,
    dotColor: Color = Color.Red,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) Color(0xFFFDE8E8) else Color.White,
        modifier = Modifier.border(
            width = 1.dp,
            color = if (isSelected) Color(0xFFD32F2F) else Color(0xFFE0E0E0),
            shape = RoundedCornerShape(20.dp)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (hasDot) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                color = if (isSelected) Color(0xFFD32F2F) else Color(0xFF49454F),
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun TaskCardItem(
    task: Task,
    onClick: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("MMM dd", Locale.getDefault()) }
    val formattedDate = remember(task.dueDate) { dateFormatter.format(Date(task.dueDate)) }

    val (badgeBg, badgeText) = when (task.priority) {
        Priority.HIGH -> Color(0xFFFFEBEE) to Color(0xFFD32F2F)
        Priority.MEDIUM -> Color(0xFFFFF3E0) to Color(0xFFE65100)
        Priority.LOW -> Color(0xE8E8F5E9) to Color(0xFF2E7D32)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Priority Tag
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeBg
                ) {
                    Text(
                        text = task.priority.name.lowercase()
                            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
                        color = badgeText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                // Due Date Tag
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "📅 $formattedDate",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = task.title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1B1F)
            )

            if (task.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = task.description,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 2
                )
            }

            if (task.attachments.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFF0F0F0))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "🖼️ 🎥 ${task.attachments.size} attachments",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        }
    }
}