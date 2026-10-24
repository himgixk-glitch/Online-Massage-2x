package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.GradientButton
import com.example.ui.components.StoryRingAvatar
import com.example.ui.theme.ChatterPurple
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.ChatterViewModel

@Composable
fun NewChatDialog(
    viewModel: ChatterViewModel,
    onDismiss: () -> Unit
) {
    val users by viewModel.users.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val contacts = remember(users, currentUser) {
        users.filter { it.uid != (currentUser?.uid ?: "user_me") }
    }

    var isGroupMode by remember { mutableStateOf(false) }
    var groupName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text(
                text = if (isGroupMode) "New Group Chat" else "New Conversation",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (!isGroupMode) {
                    // New Group Shortcut
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isGroupMode = true }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Group, contentDescription = null, tint = ChatterPurple)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Create a new group", color = ChatterPurple, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                } else {
                    OutlinedTextField(
                        value = groupName,
                        onValueChange = { groupName = it },
                        placeholder = { Text("Group Subject / Name", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ChatterPurple,
                            unfocusedBorderColor = DarkSurfaceCard,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Text(
                    text = if (isGroupMode) "Select participants below:" else "Choose a contact:",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(modifier = Modifier.height(220.dp)) {
                    items(contacts) { contact ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isGroupMode) {
                                        if (groupName.isNotBlank()) {
                                            viewModel.createChat(
                                                name = groupName,
                                                photo = "https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=400&auto=format&fit=crop&q=80",
                                                isGroup = true,
                                                participantIds = "user_me,${contact.uid}"
                                            )
                                        }
                                    } else {
                                        viewModel.createChat(
                                            name = contact.name,
                                            photo = contact.photo,
                                            isGroup = false,
                                            participantIds = "user_me,${contact.uid}"
                                        )
                                    }
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StoryRingAvatar(photoUrl = contact.photo, size = 42.dp, isOnline = contact.online)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(contact.name, color = TextPrimary, fontSize = 14.5.sp, fontWeight = FontWeight.Bold)
                                Text(contact.phone, color = TextMuted, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (isGroupMode) {
                GradientButton(
                    text = "Create Group",
                    onClick = {
                        if (groupName.isNotBlank()) {
                            viewModel.createChat(
                                name = groupName,
                                photo = "https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=400&auto=format&fit=crop&q=80",
                                isGroup = true,
                                participantIds = "user_me,user_elena,user_marcus"
                            )
                        }
                    },
                    enabled = groupName.isNotBlank()
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
