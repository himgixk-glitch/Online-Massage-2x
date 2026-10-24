package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CallType
import com.example.data.model.MessageEntity
import com.example.data.model.MessageType
import com.example.ui.components.ChatBubble
import com.example.ui.components.EncryptionNoticeCard
import com.example.ui.components.StoryRingAvatar
import com.example.ui.theme.ChatterBlue
import com.example.ui.theme.ChatterCyan
import com.example.ui.theme.ChatterGreen
import com.example.ui.theme.ChatterPink
import com.example.ui.theme.ChatterPurple
import com.example.ui.theme.ChatterRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GradientPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.ChatterViewModel
import kotlinx.coroutines.delay

val QuickEmojis = listOf("❤️", "🔥", "😂", "👍", "😮", "😢")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    chatId: String,
    viewModel: ChatterViewModel,
    onBack: () -> Unit
) {
    BackHandler {
        onBack()
    }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val chat by viewModel.getChat(chatId).collectAsStateWithLifecycle()
    val rawMessages by viewModel.getChatMessages(chatId).collectAsStateWithLifecycle()
    val replyingTo by viewModel.replyingToMessage.collectAsStateWithLifecycle()
    val editingMsg by viewModel.editingMessage.collectAsStateWithLifecycle()

    var messageInput by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var inChatSearchQuery by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var showAttachmentSheet by remember { mutableStateOf(false) }
    var showEmojiDrawer by remember { mutableStateOf(false) }

    var selectedMessageForAction by remember { mutableStateOf<MessageEntity?>(null) }
    var isRecordingVoice by remember { mutableStateOf(false) }
    var voiceRecordDuration by remember { mutableIntStateOf(0) }

    LaunchedEffect(editingMsg) {
        if (editingMsg != null) {
            messageInput = editingMsg?.text ?: ""
        }
    }

    // Voice record timer simulation
    LaunchedEffect(isRecordingVoice) {
        if (isRecordingVoice) {
            voiceRecordDuration = 0
            while (isRecordingVoice) {
                delay(1000)
                voiceRecordDuration += 1
            }
        }
    }

    val filteredMessages = remember(rawMessages, inChatSearchQuery) {
        if (inChatSearchQuery.isBlank()) rawMessages
        else rawMessages.filter { it.text.contains(inChatSearchQuery, ignoreCase = true) }
    }

    val listState = rememberLazyListState()
    LaunchedEffect(filteredMessages.size) {
        if (filteredMessages.isNotEmpty()) {
            listState.animateScrollToItem(filteredMessages.size - 1)
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        color = DarkBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .padding(horizontal = 6.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("chat_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                StoryRingAvatar(
                    photoUrl = chat?.photo ?: "",
                    size = 42.dp,
                    hasStory = false,
                    isOnline = chat?.unreadCount == 0
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = chat?.name ?: "Chat",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = when {
                            chat?.isTyping == true -> "typing..."
                            chat?.isGroup == true -> "Group • 4 members"
                            else -> "online"
                        },
                        color = if (chat?.isTyping == true) ChatterCyan else ChatterGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Video Call Button
                IconButton(
                    onClick = {
                        val currentChat = chat ?: return@IconButton
                        viewModel.startCall(
                            contactId = currentChat.chatId,
                            contactName = currentChat.name,
                            contactPhoto = currentChat.photo,
                            type = CallType.VIDEO
                        )
                    },
                    modifier = Modifier.testTag("start_video_call_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = "Video Call",
                        tint = ChatterPurple,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Audio Call Button
                IconButton(
                    onClick = {
                        val currentChat = chat ?: return@IconButton
                        viewModel.startCall(
                            contactId = currentChat.chatId,
                            contactName = currentChat.name,
                            contactPhoto = currentChat.photo,
                            type = CallType.VOICE
                        )
                    },
                    modifier = Modifier.testTag("start_voice_call_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Voice Call",
                        tint = ChatterBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Menu
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = TextSecondary
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(DarkSurfaceCard)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Search in chat", color = TextPrimary) },
                            leadingIcon = { Icon(Icons.Default.Search, null, tint = ChatterBlue) },
                            onClick = {
                                showMenu = false
                                isSearchActive = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Simulate Incoming Call", color = TextPrimary) },
                            leadingIcon = { Icon(Icons.Default.Videocam, null, tint = ChatterPurple) },
                            onClick = {
                                showMenu = false
                                viewModel.triggerIncomingCall(
                                    callerName = chat?.name ?: "Elena Rostova",
                                    callerPhoto = chat?.photo ?: "",
                                    type = CallType.VIDEO
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Block User", color = TextPrimary) },
                            leadingIcon = { Icon(Icons.Default.Block, null, tint = ChatterRed) },
                            onClick = {
                                showMenu = false
                                Toast.makeText(context, "${chat?.name} blocked", Toast.LENGTH_SHORT).show()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Report User", color = TextPrimary) },
                            leadingIcon = { Icon(Icons.Default.Flag, null, tint = TextMuted) },
                            onClick = {
                                showMenu = false
                                Toast.makeText(context, "User reported to Chatter trust & safety", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }

            // In-Chat Search Bar
            AnimatedVisibility(visible = isSearchActive) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurfaceVariant)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inChatSearchQuery,
                        onValueChange = { inChatSearchQuery = it },
                        placeholder = { Text("Search in conversation...", color = TextMuted, fontSize = 13.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ChatterPurple,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    )
                    IconButton(onClick = {
                        isSearchActive = false
                        inChatSearchQuery = ""
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Close search", tint = TextSecondary)
                    }
                }
            }

            // Message List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                item {
                    EncryptionNoticeCard()
                }

                items(filteredMessages, key = { it.id }) { message ->
                    val isOutgoing = message.senderId == "user_me"
                    ChatBubble(
                        message = message,
                        isOutgoing = isOutgoing,
                        onLongClick = {
                            selectedMessageForAction = message
                        }
                    )
                }
            }

            // Replying Banner
            AnimatedVisibility(visible = replyingTo != null) {
                replyingTo?.let { replyMsg ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurfaceCard)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(32.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(ChatterPurple)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Replying to ${replyMsg.senderName}",
                                color = ChatterPurple,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = replyMsg.text.ifEmpty { "Attachment" },
                                color = TextSecondary,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }
                        IconButton(onClick = { viewModel.setReplyingTo(null) }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel reply", tint = TextMuted)
                        }
                    }
                }
            }

            // Editing Banner
            AnimatedVisibility(visible = editingMsg != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurfaceCard)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = ChatterBlue, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Editing message...",
                        color = ChatterBlue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        viewModel.setEditingMessage(null)
                        messageInput = ""
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel edit", tint = TextMuted)
                    }
                }
            }

            // Quick Emoji Drawer
            AnimatedVisibility(visible = showEmojiDrawer) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurfaceCard)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val emojis = listOf("😊", "😂", "❤️", "🔥", "👍", "🙌", "🎉", "✨", "☕", "😎")
                    emojis.forEach { emoji ->
                        Text(
                            text = emoji,
                            fontSize = 24.sp,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    messageInput += emoji
                                }
                                .padding(4.dp)
                        )
                    }
                }
            }

            // Bottom Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Emoji Toggle Button
                IconButton(
                    onClick = { showEmojiDrawer = !showEmojiDrawer }
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEmotions,
                        contentDescription = "Emoji",
                        tint = if (showEmojiDrawer) ChatterPurple else TextSecondary
                    )
                }

                // Attachment Button
                IconButton(
                    onClick = { showAttachmentSheet = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachFile,
                        contentDescription = "Attach",
                        tint = TextSecondary
                    )
                }

                // Text Field / Voice recording banner
                if (isRecordingVoice) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(DarkSurfaceVariant)
                            .padding(horizontal = 16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(ChatterRed)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = String.format("Recording %02d:%02d", voiceRecordDuration / 60, voiceRecordDuration % 60),
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "Release to send",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                } else {
                    OutlinedTextField(
                        value = messageInput,
                        onValueChange = { messageInput = it },
                        placeholder = { Text("Message...", color = TextMuted, fontSize = 14.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ChatterPurple,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("chat_input_field")
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Send or Voice Note Button
                if (messageInput.isNotBlank()) {
                    IconButton(
                        onClick = {
                            viewModel.sendMessage(
                                chatId = chatId,
                                text = messageInput.trim(),
                                type = MessageType.TEXT
                            )
                            messageInput = ""
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(GradientPrimary)
                            .testTag("chat_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    // Hold to Record Voice Note
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .then(
                                if (isRecordingVoice) Modifier.background(ChatterRed)
                                else Modifier.background(GradientPrimary)
                            )
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        isRecordingVoice = true
                                        val released = tryAwaitRelease()
                                        if (released && voiceRecordDuration >= 1) {
                                            viewModel.sendMessage(
                                                chatId = chatId,
                                                text = "Voice message",
                                                type = MessageType.VOICE,
                                                voiceDurationSeconds = voiceRecordDuration.coerceAtLeast(4)
                                            )
                                        }
                                        isRecordingVoice = false
                                    }
                                )
                            }
                            .testTag("hold_to_record_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Hold to record",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }

    // Attachment Options Modal Bottom Sheet
    if (showAttachmentSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAttachmentSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = DarkSurfaceCard
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Share Content",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // Send Photo
                    AttachmentOptionItem(
                        icon = Icons.Default.Image,
                        label = "Gallery",
                        color = ChatterPink
                    ) {
                        showAttachmentSheet = false
                        viewModel.sendMessage(
                            chatId = chatId,
                            text = "Beautiful view today!",
                            type = MessageType.IMAGE,
                            mediaUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800&auto=format&fit=crop&q=80"
                        )
                    }

                    // Send Location
                    AttachmentOptionItem(
                        icon = Icons.Default.LocationOn,
                        label = "Location",
                        color = ChatterCyan
                    ) {
                        showAttachmentSheet = false
                        viewModel.sendMessage(
                            chatId = chatId,
                            text = "Shared Current Location: Market St, San Francisco, CA",
                            type = MessageType.LOCATION
                        )
                    }

                    // Send Document
                    AttachmentOptionItem(
                        icon = Icons.Default.Description,
                        label = "Document",
                        color = ChatterPurple
                    ) {
                        showAttachmentSheet = false
                        viewModel.sendMessage(
                            chatId = chatId,
                            text = "Chatter_Project_Architecture.pdf",
                            type = MessageType.FILE
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Message Long-Press Actions Bottom Sheet
    if (selectedMessageForAction != null) {
        val targetMessage = selectedMessageForAction!!
        val isSenderMe = targetMessage.senderId == "user_me"

        ModalBottomSheet(
            onDismissRequest = { selectedMessageForAction = null },
            sheetState = rememberModalBottomSheetState(),
            containerColor = DarkSurfaceCard
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Quick emoji reactions row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    QuickEmojis.forEach { emoji ->
                        Text(
                            text = emoji,
                            fontSize = 28.sp,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    viewModel.reactToMessage(targetMessage.id, emoji)
                                    selectedMessageForAction = null
                                }
                                .padding(6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Reply
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            viewModel.setReplyingTo(targetMessage)
                            selectedMessageForAction = null
                        }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Reply, contentDescription = null, tint = ChatterBlue)
                    Spacer(modifier = Modifier.width(14.dp))
                    Text("Reply", color = TextPrimary, fontSize = 15.sp)
                }

                // Copy Text
                if (targetMessage.type == MessageType.TEXT) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                clipboardManager.setText(AnnotatedString(targetMessage.text))
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                selectedMessageForAction = null
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, tint = ChatterCyan)
                        Spacer(modifier = Modifier.width(14.dp))
                        Text("Copy Text", color = TextPrimary, fontSize = 15.sp)
                    }
                }

                // Edit (if sent by current user)
                if (isSenderMe && targetMessage.type == MessageType.TEXT && !targetMessage.isDeleted) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.setEditingMessage(targetMessage)
                                selectedMessageForAction = null
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = ChatterPurple)
                        Spacer(modifier = Modifier.width(14.dp))
                        Text("Edit Message", color = TextPrimary, fontSize = 15.sp)
                    }
                }

                // Delete For Everyone
                if (isSenderMe && !targetMessage.isDeleted) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.deleteMessageForEveryone(targetMessage.id)
                                selectedMessageForAction = null
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = ChatterRed)
                        Spacer(modifier = Modifier.width(14.dp))
                        Text("Delete for everyone", color = ChatterRed, fontSize = 15.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun AttachmentOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f))
                .border(1.dp, color.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
