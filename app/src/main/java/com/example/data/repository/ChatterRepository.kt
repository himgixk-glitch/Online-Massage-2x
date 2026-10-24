package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.CallEntity
import com.example.data.model.CallStatus
import com.example.data.model.CallType
import com.example.data.model.ChatEntity
import com.example.data.model.MessageEntity
import com.example.data.model.MessageStatus
import com.example.data.model.MessageType
import com.example.data.model.StoryEntity
import com.example.data.model.StoryType
import com.example.data.model.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.UUID

class ChatterRepository(private val database: AppDatabase) {

    private val userDao = database.userDao()
    private val chatDao = database.chatDao()
    private val messageDao = database.messageDao()
    private val callDao = database.callDao()
    private val storyDao = database.storyDao()

    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsers()
    val currentUser: Flow<UserEntity?> = userDao.getCurrentUser()
    val allChats: Flow<List<ChatEntity>> = chatDao.getAllChats()
    val allCalls: Flow<List<CallEntity>> = callDao.getAllCalls()

    fun getChatMessages(chatId: String): Flow<List<MessageEntity>> =
        messageDao.getMessagesForChat(chatId)

    fun getChatById(chatId: String): Flow<ChatEntity?> =
        chatDao.getChatById(chatId)

    fun getActiveStories(): Flow<List<StoryEntity>> {
        val twentyFourHoursAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000L)
        return storyDao.getActiveStories(twentyFourHoursAgo)
    }

    suspend fun initializeSeedDataIfNeeded() {
        val users = allUsers.firstOrNull()
        if (users.isNullOrEmpty()) {
            val now = System.currentTimeMillis()

            // Current logged in user
            val current = UserEntity(
                uid = "user_me",
                name = "Alex Morgan",
                photo = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&auto=format&fit=crop&q=80",
                phone = "+1 (555) 019-2834",
                online = true,
                bio = "Available | Building with Chatter 🚀",
                isCurrentUser = true
            )

            // Contacts
            val contact1 = UserEntity(
                uid = "user_elena",
                name = "Elena Rostova",
                photo = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400&auto=format&fit=crop&q=80",
                phone = "+1 (555) 234-5678",
                online = true,
                bio = "Product Designer & Visual Artist ✨"
            )

            val contact2 = UserEntity(
                uid = "user_marcus",
                name = "Marcus Vance",
                photo = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400&auto=format&fit=crop&q=80",
                phone = "+1 (555) 876-5432",
                online = true,
                bio = "WebRTC & Audio Engineering Specialist 🎧"
            )

            val contact3 = UserEntity(
                uid = "user_sophia",
                name = "Sophia Chen",
                photo = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=400&auto=format&fit=crop&q=80",
                phone = "+1 (555) 998-1122",
                online = false,
                lastSeen = now - 18 * 60 * 1000L,
                bio = "Capturing moments 📸 | Coffee enthusiast ☕"
            )

            val contact4 = UserEntity(
                uid = "user_david",
                name = "David Miller",
                photo = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=400&auto=format&fit=crop&q=80",
                phone = "+1 (555) 443-8899",
                online = true,
                bio = "Always on the move 🏃‍♂️"
            )

            userDao.insertUsers(listOf(current, contact1, contact2, contact3, contact4))

            // Seed Chats
            val chat1 = ChatEntity(
                chatId = "chat_elena",
                name = "Elena Rostova",
                photo = contact1.photo,
                isGroup = false,
                participantIds = "user_me,user_elena",
                lastMessage = "Let's test the HD video call feature!",
                lastMessageTimestamp = now - 5 * 60 * 1000L,
                unreadCount = 2
            )

            val chat2 = ChatEntity(
                chatId = "chat_marcus",
                name = "Marcus Vance",
                photo = contact2.photo,
                isGroup = false,
                participantIds = "user_me,user_marcus",
                lastMessage = "Voice note (0:18)",
                lastMessageTimestamp = now - 35 * 60 * 1000L,
                unreadCount = 0
            )

            val chat3 = ChatEntity(
                chatId = "chat_group",
                name = "Chatter Core Team",
                photo = "https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=400&auto=format&fit=crop&q=80",
                isGroup = true,
                participantIds = "user_me,user_elena,user_marcus,user_sophia",
                lastMessage = "Sophia: Shared location: Innovation Hub SF",
                lastMessageTimestamp = now - 2 * 3600 * 1000L,
                unreadCount = 0
            )

            val chat4 = ChatEntity(
                chatId = "chat_sophia",
                name = "Sophia Chen",
                photo = contact3.photo,
                isGroup = false,
                participantIds = "user_me,user_sophia",
                lastMessage = "Check out this shot from yesterday!",
                lastMessageTimestamp = now - 6 * 3600 * 1000L,
                unreadCount = 0
            )

            chatDao.insertChats(listOf(chat1, chat2, chat3, chat4))

            // Seed Messages for chat_elena
            val messagesElena = listOf(
                MessageEntity(
                    id = "msg_1",
                    chatId = "chat_elena",
                    text = "Hey Alex! Have you reviewed the new dark gradient UI for Chatter?",
                    type = MessageType.TEXT,
                    senderId = "user_elena",
                    senderName = "Elena",
                    timestamp = now - 20 * 60 * 1000L,
                    status = MessageStatus.READ
                ),
                MessageEntity(
                    id = "msg_2",
                    chatId = "chat_elena",
                    text = "Yes, absolutely stunning! The Purple #7B5BFE to Blue #5B8DEF gradient looks so sleek.",
                    type = MessageType.TEXT,
                    senderId = "user_me",
                    senderName = "Alex",
                    timestamp = now - 15 * 60 * 1000L,
                    status = MessageStatus.READ,
                    reactionEmoji = "🔥"
                ),
                MessageEntity(
                    id = "msg_3",
                    chatId = "chat_elena",
                    text = "Check out the mockup preview on tablet mode",
                    type = MessageType.IMAGE,
                    senderId = "user_elena",
                    senderName = "Elena",
                    timestamp = now - 10 * 60 * 1000L,
                    mediaUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800&auto=format&fit=crop&q=80",
                    status = MessageStatus.READ
                ),
                MessageEntity(
                    id = "msg_4",
                    chatId = "chat_elena",
                    text = "Let's test the HD video call feature!",
                    type = MessageType.TEXT,
                    senderId = "user_elena",
                    senderName = "Elena",
                    timestamp = now - 5 * 60 * 1000L,
                    status = MessageStatus.DELIVERED
                )
            )

            // Seed Messages for chat_marcus (with voice note!)
            val messagesMarcus = listOf(
                MessageEntity(
                    id = "msg_m1",
                    chatId = "chat_marcus",
                    text = "Marcus, how is audio latency over WebRTC peer connection?",
                    type = MessageType.TEXT,
                    senderId = "user_me",
                    senderName = "Alex",
                    timestamp = now - 45 * 60 * 1000L,
                    status = MessageStatus.READ
                ),
                MessageEntity(
                    id = "msg_m2",
                    chatId = "chat_marcus",
                    text = "Voice message",
                    type = MessageType.VOICE,
                    senderId = "user_marcus",
                    senderName = "Marcus",
                    timestamp = now - 35 * 60 * 1000L,
                    voiceDurationSeconds = 18,
                    status = MessageStatus.READ
                )
            )

            // Seed Messages for chat_group
            val messagesGroup = listOf(
                MessageEntity(
                    id = "msg_g1",
                    chatId = "chat_group",
                    text = "Welcome to the Chatter core development channel!",
                    type = MessageType.TEXT,
                    senderId = "user_elena",
                    senderName = "Elena",
                    timestamp = now - 4 * 3600 * 1000L,
                    status = MessageStatus.READ
                ),
                MessageEntity(
                    id = "msg_g2",
                    chatId = "chat_group",
                    text = "San Francisco Innovation Hub, 450 Mission St",
                    type = MessageType.LOCATION,
                    senderId = "user_sophia",
                    senderName = "Sophia",
                    timestamp = now - 2 * 3600 * 1000L,
                    status = MessageStatus.READ
                )
            )

            messageDao.insertMessages(messagesElena + messagesMarcus + messagesGroup)

            // Seed Call History
            val call1 = CallEntity(
                callId = "call_1",
                callerId = "user_elena",
                callerName = "Elena Rostova",
                callerPhoto = contact1.photo,
                receiverId = "user_me",
                receiverName = "Alex",
                receiverPhoto = current.photo,
                type = CallType.VIDEO,
                status = CallStatus.CONNECTED,
                timestamp = now - 40 * 60 * 1000L,
                durationSeconds = 542
            )

            val call2 = CallEntity(
                callId = "call_2",
                callerId = "user_me",
                callerName = "Alex",
                callerPhoto = current.photo,
                receiverId = "user_marcus",
                receiverName = "Marcus Vance",
                receiverPhoto = contact2.photo,
                type = CallType.VOICE,
                status = CallStatus.OUTGOING,
                timestamp = now - 3 * 3600 * 1000L,
                durationSeconds = 215
            )

            val call3 = CallEntity(
                callId = "call_3",
                callerId = "user_sophia",
                callerName = "Sophia Chen",
                callerPhoto = contact3.photo,
                receiverId = "user_me",
                receiverName = "Alex",
                receiverPhoto = current.photo,
                type = CallType.VIDEO,
                status = CallStatus.MISSED,
                timestamp = now - 7 * 3600 * 1000L,
                durationSeconds = 0
            )

            callDao.insertCalls(listOf(call1, call2, call3))

            // Seed 24hr Stories
            val story1 = StoryEntity(
                storyId = "story_1",
                userId = "user_elena",
                userName = "Elena Rostova",
                userPhoto = contact1.photo,
                type = StoryType.PHOTO,
                mediaUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800&auto=format&fit=crop&q=80",
                caption = "Sunset vibes by the ocean 🌅",
                timestamp = now - 2 * 3600 * 1000L,
                viewsCount = 42
            )

            val story2 = StoryEntity(
                storyId = "story_2",
                userId = "user_marcus",
                userName = "Marcus Vance",
                userPhoto = contact2.photo,
                type = StoryType.TEXT,
                textContent = "Excited to test low-latency peer-to-peer WebRTC video streaming today! 🚀📞",
                gradientIndex = 1,
                timestamp = now - 5 * 3600 * 1000L,
                viewsCount = 28
            )

            val story3 = StoryEntity(
                storyId = "story_3",
                userId = "user_sophia",
                userName = "Sophia Chen",
                userPhoto = contact3.photo,
                type = StoryType.PHOTO,
                mediaUrl = "https://images.unsplash.com/photo-1469474968028-56623f02e42e?w=800&auto=format&fit=crop&q=80",
                caption = "Weekend wilderness escape 🌲✨",
                timestamp = now - 8 * 3600 * 1000L,
                viewsCount = 59
            )

            storyDao.insertStories(listOf(story1, story2, story3))
        }
    }

    suspend fun saveCurrentUser(name: String, phone: String, photo: String, bio: String) {
        val user = UserEntity(
            uid = "user_me",
            name = name,
            phone = phone,
            photo = photo.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&auto=format&fit=crop&q=80" },
            online = true,
            bio = bio,
            isCurrentUser = true
        )
        userDao.insertUser(user)
    }

    suspend fun sendMessage(
        chatId: String,
        text: String,
        type: MessageType = MessageType.TEXT,
        mediaUrl: String? = null,
        replyToId: String? = null,
        replyToText: String? = null,
        voiceDurationSeconds: Int = 0,
        scope: CoroutineScope
    ) {
        val now = System.currentTimeMillis()
        val messageId = "msg_${UUID.randomUUID()}"
        val user = currentUser.firstOrNull() ?: UserEntity(
            uid = "user_me",
            name = "Me",
            phone = "",
            photo = ""
        )

        val message = MessageEntity(
            id = messageId,
            chatId = chatId,
            text = text,
            type = type,
            senderId = user.uid,
            senderName = user.name,
            timestamp = now,
            status = MessageStatus.SENT,
            replyToId = replyToId,
            replyToText = replyToText,
            mediaUrl = mediaUrl,
            voiceDurationSeconds = voiceDurationSeconds
        )

        messageDao.insertMessage(message)

        // Update chat preview
        val preview = when (type) {
            MessageType.TEXT -> text
            MessageType.VOICE -> "Voice note (${voiceDurationSeconds}s)"
            MessageType.IMAGE -> "📷 Photo"
            MessageType.VIDEO -> "🎥 Video"
            MessageType.LOCATION -> "📍 Location"
            MessageType.FILE -> "📄 File"
        }
        val chat = chatDao.getChatDirect(chatId)
        if (chat != null) {
            chatDao.updateChat(
                chat.copy(
                    lastMessage = preview,
                    lastMessageTimestamp = now
                )
            )
        }

        // Realistic delivery progression: SENT -> DELIVERED -> READ -> Intelligent contact response!
        scope.launch(Dispatchers.IO) {
            delay(800)
            messageDao.updateMessage(message.copy(status = MessageStatus.DELIVERED))

            delay(1000)
            messageDao.updateMessage(message.copy(status = MessageStatus.READ))

            // If not group or if targeted to contact, show typing indicator then smart response
            chatDao.setTypingState(chatId, isTyping = true, typingText = "typing...")
            delay(1800)
            chatDao.setTypingState(chatId, isTyping = false, typingText = "")

            val replyText = generateBotReply(text, type, chat?.name ?: "Contact")
            val botMessageId = "msg_${UUID.randomUUID()}"
            val replyMessage = MessageEntity(
                id = botMessageId,
                chatId = chatId,
                text = replyText,
                type = MessageType.TEXT,
                senderId = "contact_$chatId",
                senderName = chat?.name ?: "Contact",
                timestamp = System.currentTimeMillis(),
                status = MessageStatus.READ
            )
            messageDao.insertMessage(replyMessage)

            val updatedChat = chatDao.getChatDirect(chatId)
            if (updatedChat != null) {
                chatDao.updateChat(
                    updatedChat.copy(
                        lastMessage = replyText,
                        lastMessageTimestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    private fun generateBotReply(userText: String, type: MessageType, contactName: String): String {
        val lower = userText.lowercase()
        return when {
            type == MessageType.VOICE -> "Great voice note! Audio was crystal clear. 🎧👍"
            type == MessageType.IMAGE -> "Love the picture! Looks fantastic ✨📸"
            type == MessageType.LOCATION -> "Thanks for sharing your location! Heading over now 📍🚀"
            lower.contains("call") || lower.contains("video") -> "Ready for a video call anytime! Just tap the camera icon up top 📹"
            lower.contains("hello") || lower.contains("hi") || lower.contains("hey") -> "Hey! Great to hear from you. How are things going?"
            lower.contains("status") || lower.contains("story") -> "Did you see my new 24-hour status update? Check it out in the Status tab! 🌟"
            lower.contains("chatter") -> "Chatter is awesome! Real-time chats, voice notes and instant video calling all in one."
            lower.contains("where") -> "I'm currently at the studio finishing up some work. What about you?"
            lower.contains("thanks") || lower.contains("thank") -> "You're most welcome! 😊"
            else -> "Got it! That sounds great. Let's catch up more in a bit!"
        }
    }

    suspend fun reactToMessage(messageId: String, emoji: String?) {
        messageDao.updateReaction(messageId, emoji)
    }

    suspend fun editMessage(messageId: String, newText: String) {
        messageDao.editMessage(messageId, newText)
    }

    suspend fun deleteForEveryone(messageId: String) {
        messageDao.deleteForEveryone(messageId)
    }

    suspend fun deleteMessage(messageId: String) {
        messageDao.deleteMessage(messageId)
    }

    suspend fun markChatAsRead(chatId: String) {
        chatDao.markChatAsRead(chatId)
    }

    suspend fun recordCall(
        callerId: String,
        callerName: String,
        callerPhoto: String,
        receiverId: String,
        receiverName: String,
        receiverPhoto: String,
        type: CallType,
        status: CallStatus,
        durationSeconds: Int
    ) {
        val call = CallEntity(
            callId = "call_${UUID.randomUUID()}",
            callerId = callerId,
            callerName = callerName,
            callerPhoto = callerPhoto,
            receiverId = receiverId,
            receiverName = receiverName,
            receiverPhoto = receiverPhoto,
            type = type,
            status = status,
            timestamp = System.currentTimeMillis(),
            durationSeconds = durationSeconds
        )
        callDao.insertCall(call)
    }

    suspend fun clearCallHistory() {
        callDao.clearCalls()
    }

    suspend fun postStory(
        type: StoryType,
        mediaUrl: String?,
        textContent: String?,
        gradientIndex: Int,
        caption: String?
    ) {
        val user = currentUser.firstOrNull() ?: UserEntity(
            uid = "user_me",
            name = "Me",
            phone = "",
            photo = ""
        )
        val story = StoryEntity(
            storyId = "story_${UUID.randomUUID()}",
            userId = user.uid,
            userName = user.name,
            userPhoto = user.photo,
            type = type,
            mediaUrl = mediaUrl,
            textContent = textContent,
            gradientIndex = gradientIndex,
            timestamp = System.currentTimeMillis(),
            caption = caption,
            viewsCount = 1
        )
        storyDao.insertStory(story)
    }

    suspend fun markStoryViewed(storyId: String) {
        storyDao.markStoryViewed(storyId)
    }

    suspend fun deleteStory(storyId: String) {
        storyDao.deleteStory(storyId)
    }

    suspend fun createNewChat(name: String, photo: String, isGroup: Boolean, participantIds: String): String {
        val chatId = "chat_${UUID.randomUUID()}"
        val chat = ChatEntity(
            chatId = chatId,
            name = name,
            photo = photo.ifEmpty { "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400&auto=format&fit=crop&q=80" },
            isGroup = isGroup,
            participantIds = participantIds,
            lastMessage = if (isGroup) "Group created" else "Chat started",
            lastMessageTimestamp = System.currentTimeMillis(),
            unreadCount = 0
        )
        chatDao.insertChat(chat)
        return chatId
    }
}
