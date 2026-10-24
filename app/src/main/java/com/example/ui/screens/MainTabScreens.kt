package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CallEntity
import com.example.data.model.CallStatus
import com.example.data.model.CallType
import com.example.data.model.ChatEntity
import com.example.data.model.StoryEntity
import com.example.data.model.StoryType
import com.example.data.model.UserEntity
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
import com.example.ui.viewmodel.ActiveStoryGroup
import com.example.ui.viewmodel.ChatterViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatsTabScreen(
    viewModel: ChatterViewModel,
    onSelectChat: (String) -> Unit
) {
    val chats by viewModel.chats.collectAsStateWithLifecycle()
    val stories by viewModel.stories.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }

    val filteredChats = remember(chats, searchQuery) {
        if (searchQuery.isBlank()) chats
        else chats.filter { it.name.contains(searchQuery, ignoreCase = true) || it.lastMessage.contains(searchQuery, ignoreCase = true) }
    }

    // Group active 24h stories by author
    val storiesByUser = remember(stories) {
        stories.groupBy { it.userId }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Search Bar
            item {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search chats or messages...", color = TextMuted, fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = TextSecondary)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ChatterPurple,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = DarkSurfaceCard,
                        unfocusedContainerColor = DarkSurfaceCard
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("chats_search_input")
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Top 24-Hour Stories / Status Reel
            item {
                Text(
                    text = "Status Updates",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 18.dp)
                ) {
                    // Current User Status Add Button
                    item {
                        val myStories = stories.filter { it.userId == (currentUser?.uid ?: "user_me") }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable {
                                    if (myStories.isNotEmpty()) {
                                        viewModel.openStoryViewer(
                                            ActiveStoryGroup(
                                                userId = currentUser?.uid ?: "user_me",
                                                userName = "My Status",
                                                userPhoto = currentUser?.photo ?: "",
                                                stories = myStories
                                            )
                                        )
                                    } else {
                                        viewModel.openCreateStoryDialog()
                                    }
                                }
                                .testTag("my_status_reel_item")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                StoryRingAvatar(
                                    photoUrl = currentUser?.photo ?: "",
                                    size = 60.dp,
                                    hasStory = myStories.isNotEmpty(),
                                    allViewed = false
                                )
                                if (myStories.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(ChatterPurple)
                                            .border(2.dp, DarkBackground, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Add status",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "My Status",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Contact Stories
                    items(storiesByUser.entries.filter { it.key != (currentUser?.uid ?: "user_me") }.toList()) { entry ->
                        val contactStories = entry.value
                        val firstStory = contactStories.firstOrNull() ?: return@items
                        val allViewed = contactStories.all { it.isViewed }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable {
                                    viewModel.openStoryViewer(
                                        ActiveStoryGroup(
                                            userId = entry.key,
                                            userName = firstStory.userName,
                                            userPhoto = firstStory.userPhoto,
                                            stories = contactStories
                                        )
                                    )
                                }
                                .testTag("contact_story_${entry.key}")
                        ) {
                            StoryRingAvatar(
                                photoUrl = firstStory.userPhoto,
                                size = 60.dp,
                                hasStory = true,
                                allViewed = allViewed
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = firstStory.userName.split(" ").firstOrNull() ?: firstStory.userName,
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Recent Conversations Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Chats",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${filteredChats.size} chats",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }

            // Chat Items
            items(filteredChats, key = { it.chatId }) { chat ->
                ChatItemRow(
                    chat = chat,
                    onClick = { onSelectChat(chat.chatId) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // New Chat FAB
        FloatingActionButton(
            onClick = { viewModel.openNewChatDialog() },
            containerColor = ChatterPurple,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_new_chat")
        ) {
            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "New Chat")
        }
    }
}

@Composable
fun ChatItemRow(
    chat: ChatEntity,
    onClick: () -> Unit
) {
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(chat.lastMessageTimestamp))

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("chat_item_${chat.chatId}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StoryRingAvatar(
                photoUrl = chat.photo,
                size = 52.dp,
                hasStory = false,
                isOnline = chat.unreadCount == 0
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = chat.name,
                        color = TextPrimary,
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formattedTime,
                        color = if (chat.unreadCount > 0) ChatterCyan else TextMuted,
                        fontSize = 11.5.sp,
                        fontWeight = if (chat.unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (chat.isTyping) "typing..." else chat.lastMessage,
                        color = when {
                            chat.isTyping -> ChatterCyan
                            chat.unreadCount > 0 -> TextPrimary
                            else -> TextSecondary
                        },
                        fontSize = 13.5.sp,
                        fontWeight = if (chat.unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (chat.unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .clip(CircleShape)
                                .background(GradientPrimary)
                                .padding(horizontal = 7.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${chat.unreadCount}",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CallsTabScreen(
    viewModel: ChatterViewModel
) {
    val calls by viewModel.calls.collectAsStateWithLifecycle()
    var selectedFilter by remember { mutableIntStateOf(0) } // 0: All, 1: Missed

    val filteredCalls = remember(calls, selectedFilter) {
        if (selectedFilter == 0) calls
        else calls.filter { it.status == CallStatus.MISSED }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedFilter == 0,
                            onClick = { selectedFilter = 0 },
                            label = { Text("All (${calls.size})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ChatterPurple,
                                selectedLabelColor = Color.White,
                                containerColor = DarkSurfaceCard,
                                labelColor = TextSecondary
                            )
                        )
                        FilterChip(
                            selected = selectedFilter == 1,
                            onClick = { selectedFilter = 1 },
                            label = { Text("Missed") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ChatterRed,
                                selectedLabelColor = Color.White,
                                containerColor = DarkSurfaceCard,
                                labelColor = TextSecondary
                            )
                        )
                    }

                    if (calls.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearCallHistory() }) {
                            Icon(Icons.Default.ClearAll, contentDescription = "Clear", tint = TextMuted)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            if (filteredCalls.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Call, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No call logs yet", color = TextSecondary, fontSize = 15.sp)
                        }
                    }
                }
            } else {
                items(filteredCalls, key = { it.callId }) { call ->
                    CallHistoryRow(
                        call = call,
                        onCallBack = {
                            viewModel.startCall(
                                contactId = call.callerId,
                                contactName = call.callerName,
                                contactPhoto = call.callerPhoto,
                                type = call.type
                            )
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Quick Call FAB
        FloatingActionButton(
            onClick = {
                viewModel.startCall(
                    contactId = "user_elena",
                    contactName = "Elena Rostova",
                    contactPhoto = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400&auto=format&fit=crop&q=80",
                    type = CallType.VIDEO
                )
            },
            containerColor = ChatterPurple,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_quick_call")
        ) {
            Icon(Icons.Default.Videocam, contentDescription = "New Call")
        }
    }
}

@Composable
fun CallHistoryRow(
    call: CallEntity,
    onCallBack: () -> Unit
) {
    val timeFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(call.timestamp))

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("call_row_${call.callId}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StoryRingAvatar(
                photoUrl = call.callerPhoto,
                size = 46.dp,
                hasStory = false
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = call.callerName,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (call.status) {
                            CallStatus.MISSED -> Icons.AutoMirrored.Filled.CallMissed
                            CallStatus.INCOMING, CallStatus.CONNECTED -> Icons.AutoMirrored.Filled.CallReceived
                            CallStatus.OUTGOING, CallStatus.ENDED -> Icons.AutoMirrored.Filled.CallMade
                        },
                        contentDescription = null,
                        tint = when (call.status) {
                            CallStatus.MISSED -> ChatterRed
                            CallStatus.INCOMING, CallStatus.CONNECTED -> ChatterGreen
                            else -> ChatterPurple
                        },
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$formattedTime ${if (call.durationSeconds > 0) "(${call.durationSeconds / 60}m ${call.durationSeconds % 60}s)" else ""}",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }

            IconButton(
                onClick = onCallBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(ChatterPurple.copy(alpha = 0.15f))
            ) {
                Icon(
                    imageVector = if (call.type == CallType.VIDEO) Icons.Default.Videocam else Icons.Default.Call,
                    contentDescription = "Call back",
                    tint = ChatterPurple,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// Dedicated Stories Tab Section (Requested by user!)
@Composable
fun StoriesTabScreen(
    viewModel: ChatterViewModel
) {
    val stories by viewModel.stories.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    val myStories = remember(stories, currentUser) {
        stories.filter { it.userId == (currentUser?.uid ?: "user_me") }
    }

    val contactStoriesGrouped = remember(stories, currentUser) {
        stories.filter { it.userId != (currentUser?.uid ?: "user_me") }.groupBy { it.userId }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // My Status Section
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "My Status",
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.clickable {
                            if (myStories.isNotEmpty()) {
                                viewModel.openStoryViewer(
                                    ActiveStoryGroup(
                                        userId = currentUser?.uid ?: "user_me",
                                        userName = "My Status",
                                        userPhoto = currentUser?.photo ?: "",
                                        stories = myStories
                                    )
                                )
                            } else {
                                viewModel.openCreateStoryDialog()
                            }
                        }
                    ) {
                        StoryRingAvatar(
                            photoUrl = currentUser?.photo ?: "",
                            size = 56.dp,
                            hasStory = myStories.isNotEmpty()
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "My Status",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (myStories.isNotEmpty()) "${myStories.size} active update(s) • Tap to view" else "Tap to add 24h status update",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }

                    // Quick Action Buttons
                    Row {
                        IconButton(
                            onClick = { viewModel.openCreateStoryDialog() },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(DarkSurfaceVariant)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Add Text Status", tint = ChatterCyan, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = { viewModel.openCreateStoryDialog() },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(GradientPrimary)
                        ) {
                            Icon(Icons.Default.Image, contentDescription = "Add Photo Status", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Recent Updates Section (24 Hours)
        item {
            Text(
                text = "Recent Updates (Disappears in 24 hours)",
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (contactStoriesGrouped.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                ) {
                    Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No contact updates right now", color = TextMuted, fontSize = 14.sp)
                    }
                }
            }
        } else {
            items(contactStoriesGrouped.entries.toList(), key = { it.key }) { entry ->
                val userStories = entry.value
                val firstStory = userStories.firstOrNull() ?: return@items
                val allViewed = userStories.all { it.isViewed }
                val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.openStoryViewer(
                                ActiveStoryGroup(
                                    userId = entry.key,
                                    userName = firstStory.userName,
                                    userPhoto = firstStory.userPhoto,
                                    stories = userStories
                                )
                            )
                        }
                        .testTag("story_group_${entry.key}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StoryRingAvatar(
                            photoUrl = firstStory.userPhoto,
                            size = 54.dp,
                            hasStory = true,
                            allViewed = allViewed
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = firstStory.userName,
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "${timeFormat.format(Date(firstStory.timestamp))} • ${userStories.size} story",
                                color = if (allViewed) TextMuted else ChatterCyan,
                                fontSize = 12.sp,
                                fontWeight = if (allViewed) FontWeight.Normal else FontWeight.SemiBold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurfaceVariant)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (firstStory.type == StoryType.TEXT) "Text" else "Photo",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// Contacts Tab Screen
@Composable
fun ContactsTabScreen(
    viewModel: ChatterViewModel,
    onSelectChat: (String) -> Unit
) {
    val users by viewModel.users.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    val contacts = remember(users, currentUser) {
        users.filter { it.uid != (currentUser?.uid ?: "user_me") }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Contacts on Chatter (${contacts.size})",
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 10.dp)
            )
        }

        items(contacts, key = { it.uid }) { contact ->
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StoryRingAvatar(
                        photoUrl = contact.photo,
                        size = 50.dp,
                        hasStory = false,
                        isOnline = contact.online
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = contact.name,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = contact.bio,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Direct Action Buttons
                    Row {
                        IconButton(
                            onClick = {
                                val chatId = "chat_${contact.uid.removePrefix("user_")}"
                                onSelectChat(chatId)
                            }
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chat", tint = ChatterPurple, modifier = Modifier.size(20.dp))
                        }
                        IconButton(
                            onClick = {
                                viewModel.startCall(contact.uid, contact.name, contact.photo, CallType.VOICE)
                            }
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Audio Call", tint = ChatterBlue, modifier = Modifier.size(20.dp))
                        }
                        IconButton(
                            onClick = {
                                viewModel.startCall(contact.uid, contact.name, contact.photo, CallType.VIDEO)
                            }
                        ) {
                            Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = ChatterPink, modifier = Modifier.size(22.dp))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// Settings Tab Screen
@Composable
fun SettingsTabScreen(
    viewModel: ChatterViewModel
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    var notificationsEnabled by remember { mutableStateOf(true) }
    var onlineStatusOnline by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))

            // User Profile Card
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StoryRingAvatar(
                        photoUrl = currentUser?.photo ?: "",
                        size = 64.dp,
                        hasStory = true
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentUser?.name ?: "Alex Morgan",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currentUser?.phone ?: "+1 (555) 019-2834",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentUser?.bio ?: "Available | Building with Chatter 🚀",
                            color = ChatterCyan,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Security & Privacy Card
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ChatterPurple.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = ChatterPurple, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("End-to-End Encryption", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text("Enabled for all chats, calls & status updates", color = ChatterGreen, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Preferences
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = ChatterBlue)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Push Notifications", color = TextPrimary, fontSize = 14.sp)
                        }
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ChatterPurple)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = ChatterGreen)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Appear Online to Contacts", color = TextPrimary, fontSize = 14.sp)
                        }
                        Switch(
                            checked = onlineStatusOnline,
                            onCheckedChange = { onlineStatusOnline = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ChatterGreen)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tech Stack & Info Card
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("About Chatter", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Version 1.0 • Real-time WebRTC Calling, Audio Messaging, 24h Stories & End-to-End Encrypted Chat.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
