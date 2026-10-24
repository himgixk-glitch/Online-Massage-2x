package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.CallStatus
import com.example.data.model.CallType
import com.example.data.model.MessageStatus
import com.example.data.model.MessageType
import com.example.data.model.StoryType

class Converters {
    @TypeConverter
    fun fromMessageType(value: MessageType): String = value.name

    @TypeConverter
    fun toMessageType(value: String): MessageType = runCatching { MessageType.valueOf(value) }.getOrDefault(MessageType.TEXT)

    @TypeConverter
    fun fromMessageStatus(value: MessageStatus): String = value.name

    @TypeConverter
    fun toMessageStatus(value: String): MessageStatus = runCatching { MessageStatus.valueOf(value) }.getOrDefault(MessageStatus.SENT)

    @TypeConverter
    fun fromCallType(value: CallType): String = value.name

    @TypeConverter
    fun toCallType(value: String): CallType = runCatching { CallType.valueOf(value) }.getOrDefault(CallType.VOICE)

    @TypeConverter
    fun fromCallStatus(value: CallStatus): String = value.name

    @TypeConverter
    fun toCallStatus(value: String): CallStatus = runCatching { CallStatus.valueOf(value) }.getOrDefault(CallStatus.CONNECTED)

    @TypeConverter
    fun fromStoryType(value: StoryType): String = value.name

    @TypeConverter
    fun toStoryType(value: String): StoryType = runCatching { StoryType.valueOf(value) }.getOrDefault(StoryType.PHOTO)
}
