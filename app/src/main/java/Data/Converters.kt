package com.example.taskflow.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun fromAttachmentList(value: List<Attachment>): String = Gson().toJson(value)

    @TypeConverter
    fun toAttachmentList(value: String): List<Attachment> {
        val listType = object : TypeToken<List<Attachment>>() {}.type
        return Gson().fromJson(value, listType) ?: emptyList()
    }
}