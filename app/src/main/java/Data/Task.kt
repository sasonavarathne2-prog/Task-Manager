package com.example.taskflow.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Priority { HIGH, MEDIUM, LOW }

enum class AttachmentType { IMAGE, VIDEO, DOC }

data class Attachment(
    val uri: String,
    val type: AttachmentType,
    val name: String
)

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val priority: Priority,
    val dueDate: Long,
    val isCompleted: Boolean = false,
    val attachments: List<Attachment> = emptyList()
)