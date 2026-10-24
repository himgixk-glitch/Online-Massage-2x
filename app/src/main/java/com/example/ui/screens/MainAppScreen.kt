package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.StoryRingAvatar
import com.example.ui.theme.ChatterBlue
import com.example.ui.theme.ChatterCyan
import com.example.ui.theme.ChatterPurple
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.ChatterViewModel
import com.example.ui.viewmodel.MainTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: ChatterViewModel
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val selectedChatId by viewModel.selectedChatId.collectAsStateWithLifecycle()
    val activeCall by viewModel.activeCall.collectAsStateWithLifecycle()
    val activeStoryGroup by viewModel.activeStoryGroup.collectAsStateWithLifecycle()
    val isCreateStoryOpen by viewModel.isCreateStoryDialogOpen.collectAsStateWithLifecycle()
    val isNewChatOpen by viewModel.isNewChatDialogOpen.collectAsStateWithLifecycle()
    val chats by viewModel.chats.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    val totalUnreadCount = chats.sumOf { it.unreadCount }

    Box(modifier = Modifier.fillMaxSize()) {
        if (selectedChatId != null) {
            // Full Screen Chat Detail
            ChatDetailScreen(
                chatId = selectedChatId!!,
                viewModel = viewModel,
                onBack = { viewModel.selectChat(null) }
            )
        } else {
            // Main App with Top App Bar and Bottom Navigation
            Scaffold(
                containerColor = DarkBackground,
                topBar = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkBackground)
                            .statusBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (currentTab) {
                                MainTab.CHATS -> "Chatter"
                                MainTab.CALLS -> "Calls"
                                MainTab.STORIES -> "Status"
                                MainTab.CONTACTS -> "Contacts"
                                MainTab.SETTINGS -> "Settings"
                            },
                            color = TextPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )

                        StoryRingAvatar(
                            photoUrl = currentUser?.photo ?: "",
                            size = 36.dp,
                            hasStory = false,
                            onClick = { viewModel.selectTab(MainTab.SETTINGS) }
                        )
                    }
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = DarkSurface,
                        contentColor = TextPrimary,
                        modifier = Modifier.navigationBarsPadding()
                    ) {
                        // Chats Tab
                        NavigationBarItem(
                            selected = currentTab == MainTab.CHATS,
                            onClick = { viewModel.selectTab(MainTab.CHATS) },
                            icon = {
                                if (totalUnreadCount > 0) {
                                    BadgedBox(badge = { Badge { Text("$totalUnreadCount") } }) {
                                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chats")
                                    }
                                } else {
                                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chats")
                                }
                            },
                            label = { Text("Chats", fontSize = 11.5.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = Color.White,
                                indicatorColor = ChatterPurple,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted
                            ),
                            modifier = Modifier.testTag("nav_tab_chats")
                        )

                        // Calls Tab
                        NavigationBarItem(
                            selected = currentTab == MainTab.CALLS,
                            onClick = { viewModel.selectTab(MainTab.CALLS) },
                            icon = { Icon(Icons.Default.Call, contentDescription = "Calls") },
                            label = { Text("Calls", fontSize = 11.5.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = Color.White,
                                indicatorColor = ChatterPurple,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted
                            ),
                            modifier = Modifier.testTag("nav_tab_calls")
                        )

                        // Status / Stories Tab (Dedicated 24h section)
                        NavigationBarItem(
                            selected = currentTab == MainTab.STORIES,
                            onClick = { viewModel.selectTab(MainTab.STORIES) },
                            icon = { Icon(Icons.Default.DonutLarge, contentDescription = "Status") },
                            label = { Text("Status", fontSize = 11.5.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = Color.White,
                                indicatorColor = ChatterPurple,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted
                            ),
                            modifier = Modifier.testTag("nav_tab_stories")
                        )

                        // Contacts Tab
                        NavigationBarItem(
                            selected = currentTab == MainTab.CONTACTS,
                            onClick = { viewModel.selectTab(MainTab.CONTACTS) },
                            icon = { Icon(Icons.Default.Contacts, contentDescription = "Contacts") },
                            label = { Text("Contacts", fontSize = 11.5.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = Color.White,
                                indicatorColor = ChatterPurple,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted
                            ),
                            modifier = Modifier.testTag("nav_tab_contacts")
                        )

                        // Settings Tab
                        NavigationBarItem(
                            selected = currentTab == MainTab.SETTINGS,
                            onClick = { viewModel.selectTab(MainTab.SETTINGS) },
                            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                            label = { Text("Settings", fontSize = 11.5.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = Color.White,
                                indicatorColor = ChatterPurple,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted
                            ),
                            modifier = Modifier.testTag("nav_tab_settings")
                        )
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    when (currentTab) {
                        MainTab.CHATS -> ChatsTabScreen(
                            viewModel = viewModel,
                            onSelectChat = { viewModel.selectChat(it) }
                        )
                        MainTab.CALLS -> CallsTabScreen(viewModel = viewModel)
                        MainTab.STORIES -> StoriesTabScreen(viewModel = viewModel)
                        MainTab.CONTACTS -> ContactsTabScreen(
                            viewModel = viewModel,
                            onSelectChat = { viewModel.selectChat(it) }
                        )
                        MainTab.SETTINGS -> SettingsTabScreen(viewModel = viewModel)
                    }
                }
            }
        }

        // Active Calling Screen (Overlay)
        if (activeCall != null) {
            val call = activeCall!!
            if (call.isIncoming && !call.isAnswered) {
                IncomingCallDialog(call = call, viewModel = viewModel)
            } else {
                CallScreen(call = call, viewModel = viewModel)
            }
        }

        // Active Story Viewer Screen (Overlay)
        if (activeStoryGroup != null) {
            StoryViewerScreen(
                storyGroup = activeStoryGroup!!,
                viewModel = viewModel,
                onClose = { viewModel.closeStoryViewer() }
            )
        }

        // Create Story / Status Dialog
        if (isCreateStoryOpen) {
            CreateStoryDialog(
                onDismiss = { viewModel.closeCreateStoryDialog() },
                onPostStory = { type, mediaUrl, textContent, gradientIndex, caption ->
                    viewModel.postStory(type, mediaUrl, textContent, gradientIndex, caption)
                }
            )
        }

        // New Chat Dialog
        if (isNewChatOpen) {
            NewChatDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.closeNewChatDialog() }
            )
        }
    }
}
