package com.example.taskflow.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taskflow.data.Attachment
import com.example.taskflow.data.AttachmentType
import com.example.taskflow.data.Priority
import com.example.taskflow.data.Task
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaskAddEditScreen(
    taskId: Long?,
    viewModel: TaskViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val tasks by viewModel.allTasks.collectAsState()
    val existingTask = remember(tasks, taskId) { tasks.find { it.id == taskId } }

    var title by remember(existingTask) { mutableStateOf(existingTask?.title ?: "") }
    var description by remember(existingTask) { mutableStateOf(existingTask?.description ?: "") }
    var selectedPriority by remember(existingTask) { mutableStateOf(existingTask?.priority ?: Priority.HIGH) }
    var selectedDueDate by remember(existingTask) { mutableLongStateOf(existingTask?.dueDate ?: System.currentTimeMillis()) }
    var isCompleted by remember(existingTask) { mutableStateOf(existingTask?.isCompleted ?: false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var attachments by remember(existingTask) {
        mutableStateOf(existingTask?.attachments ?: emptyList())
    }

    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDueDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        selectedDueDate = it
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Attachment Picker Launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            try {
                context.contentResolver.takePersistableUriPermission(
                    selectedUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) { }

            val name = selectedUri.lastPathSegment?.substringAfterLast('/') ?: "file_${System.currentTimeMillis()}"
            val mimeType = context.contentResolver.getType(selectedUri) ?: ""

            val attachmentType = if (mimeType.startsWith("image/")) {
                AttachmentType.entries.firstOrNull { it.name == "IMAGE" } ?: AttachmentType.entries.first()
            } else {
                AttachmentType.entries.firstOrNull { it.name != "IMAGE" } ?: AttachmentType.entries.first()
            }

            val newAttachment = Attachment(
                uri = selectedUri.toString(),
                name = name,
                type = attachmentType
            )
            attachments = attachments + newAttachment
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (taskId == null) "Add Task" else "Edit Task",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1B1F)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF1C1B1F)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Task Title Input
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Task Title",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF49454F)
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF1C1B1F),
                        unfocusedTextColor = Color(0xFF1C1B1F),
                        focusedBorderColor = Color(0xFF6750A4),
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedContainerColor = Color(0xFFF6F6F8),
                        unfocusedContainerColor = Color(0xFFF6F6F8)
                    ),
                    singleLine = true
                )
            }

            // Description Input
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Description",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF49454F)
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF1C1B1F),
                        unfocusedTextColor = Color(0xFF1C1B1F),
                        focusedBorderColor = Color(0xFF6750A4),
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedContainerColor = Color(0xFFF6F6F8),
                        unfocusedContainerColor = Color(0xFFF6F6F8)
                    )
                )
            }

            // Deadline Date Picker Field
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Deadline Date",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF49454F)
                )
                OutlinedTextField(
                    value = dateFormatter.format(Date(selectedDueDate)),
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Select Date",
                                tint = Color(0xFF6750A4)
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF1C1B1F),
                        unfocusedTextColor = Color(0xFF1C1B1F),
                        focusedBorderColor = Color(0xFF6750A4),
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedContainerColor = Color(0xFFF6F6F8),
                        unfocusedContainerColor = Color(0xFFF6F6F8)
                    )
                )
            }

            // Priority Level Selection
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Priority Level",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF49454F)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PriorityTag(
                        text = "High",
                        isSelected = selectedPriority == Priority.HIGH,
                        dotColor = Color(0xFFD32F2F),
                        selectedBgColor = Color(0xFFFDE8E8),
                        selectedBorderColor = Color(0xFFD32F2F),
                        selectedTextColor = Color(0xFFD32F2F),
                        onClick = { selectedPriority = Priority.HIGH }
                    )
                    PriorityTag(
                        text = "Medium",
                        isSelected = selectedPriority == Priority.MEDIUM,
                        dotColor = Color(0xFFF57C00),
                        selectedBgColor = Color(0xFFFFF3E0),
                        selectedBorderColor = Color(0xFFF57C00),
                        selectedTextColor = Color(0xFFE65100),
                        onClick = { selectedPriority = Priority.MEDIUM }
                    )
                    PriorityTag(
                        text = "Low",
                        isSelected = selectedPriority == Priority.LOW,
                        dotColor = Color(0xFF388E3C),
                        selectedBgColor = Color(0xFFE8F5E9),
                        selectedBorderColor = Color(0xFF388E3C),
                        selectedTextColor = Color(0xFF2E7D32),
                        onClick = { selectedPriority = Priority.LOW }
                    )
                }
            }

            // Task Status Switch (Completed Toggle)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mark as Completed",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF49454F)
                )
                Switch(
                    checked = isCompleted,
                    onCheckedChange = { isCompleted = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF6750A4)
                    )
                )
            }

            // Manage Attachments Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Manage Attachments",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF49454F)
                    )

                    TextButton(
                        onClick = { filePickerLauncher.launch("*/*") }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Attachment",
                            tint = Color(0xFF6750A4),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Add File",
                            color = Color(0xFF6750A4),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (attachments.isEmpty()) {
                    Text(
                        text = "No attachments added yet.",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        attachments.forEach { attachment ->
                            AttachmentChip(
                                attachment = attachment,
                                onClick = { openAttachment(context, attachment) },
                                onRemove = {
                                    attachments = attachments.filter { it != attachment }
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Save Action Button
            Button(
                onClick = {
                    val taskToSave = Task(
                        id = existingTask?.id ?: 0L,
                        title = title,
                        description = description,
                        priority = selectedPriority,
                        dueDate = selectedDueDate,
                        isCompleted = isCompleted,
                        attachments = attachments
                    )
                    if (existingTask == null) {
                        viewModel.addTask(taskToSave)
                    } else {
                        viewModel.updateTask(taskToSave)
                    }
                    onBackClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
            ) {
                Text(
                    text = "Save Changes",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            if (existingTask != null) {
                OutlinedButton(
                    onClick = {
                        viewModel.deleteTask(existingTask)
                        onBackClick()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    border = BorderStroke(1.dp, Color(0xFFB3261E)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFB3261E))
                ) {
                    Text(
                        text = "Delete Task",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB3261E)
                    )
                }
            }
        }
    }
}

fun openAttachment(context: Context, attachment: Attachment) {
    try {
        val uri = Uri.parse(attachment.uri)
        val type = context.contentResolver.getType(uri) ?: "*/*"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, type)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Unable to open file", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun PriorityTag(
    text: String,
    isSelected: Boolean,
    dotColor: Color,
    selectedBgColor: Color,
    selectedBorderColor: Color,
    selectedTextColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) selectedBgColor else Color(0xFFF6F6F8),
        modifier = Modifier.border(
            width = 1.dp,
            color = if (isSelected) selectedBorderColor else Color(0xFFE0E0E0),
            shape = RoundedCornerShape(20.dp)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                color = if (isSelected) selectedTextColor else Color(0xFF1C1B1F),
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun AttachmentChip(
    attachment: Attachment,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    val fileName = attachment.name.ifEmpty { attachment.uri }
    val isImage = attachment.type.name.contains("IMAGE", ignoreCase = true)
    val icon = if (isImage) "🖼️" else "📄"

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF6F6F8),
        modifier = Modifier
            .border(width = 1.dp, color = Color(0xFFE0E0E0), shape = RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = fileName, fontSize = 13.sp, color = Color(0xFF1C1B1F))
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove Attachment",
                tint = Color.Gray,
                modifier = Modifier
                    .size(16.dp)
                    .clickable { onRemove() }
            )
        }
    }
}