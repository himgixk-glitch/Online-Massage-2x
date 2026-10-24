package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val uid: String,
    val name: String,
    val photo: String,
    val phone: String,
    val online: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis(),
    val bio: String = "Hey there! I am using Chatter.",
    val isCurrentUser: Boolean = false,
    val isBlocked: Boolean = false
)

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val chatId: String,
    val name: String,
    val photo: String,
    val isGroup: Boolean = false,
    val participantIds: String = "",
    val lastMessage: String = "",
    val lastMessageTimestamp: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0,
    val isTyping: Boolean = false,
    val typingText: String = ""
)

enum class MessageType {
    TEXT,
    IMAGE,
    VIDEO,
    FILE,
    VOICE,
    LOCATION
}

enum class MessageStatus {
    SENT,
    DELIVERED,
    READ
}

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val chatId: String,
    val text: String,
    val type: MessageType = MessageType.TEXT,
    val senderId: String,
    val senderName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: MessageStatus = MessageStatus.READ,
    val replyToId: String? = null,
    val replyToText: String? = null,
    val mediaUrl: String? = null,
    val voiceDurationSeconds: Int = 0,
    val reactionEmoji: String? = null,
    val isEdited: Boolean = false,
    val isDeleted: Boolean = false
)

enum class CallType {
    VOICE,
    VIDEO
}

enum class CallStatus {
    MISSED,
    INCOMING,
    OUTGOING,
    CONNECTED,
    ENDED
}

@Entity(tableName = "calls")
data class CallEntity(
    @PrimaryKey val callId: String,
    val callerId: String,
    val callerName: String,
    val callerPhoto: String,
    val receiverId: String,
    val receiverName: String,
    val receiverPhoto: String,
    val type: CallType = CallType.VIDEO,
    val status: CallStatus = CallStatus.CONNECTED,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 0
)

enum class StoryType {
    TEXT,
    PHOTO,
    VIDEO
}

@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey val storyId: String,
    val userId: String,
    val userName: String,
    val userPhoto: String,
    val type: StoryType = StoryType.PHOTO,
    val mediaUrl: String? = null,
    val textContent: String? = null,
    val gradientIndex: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val caption: String? = null,
    val viewsCount: Int = 0,
    val isViewed: Boolean = false
)
