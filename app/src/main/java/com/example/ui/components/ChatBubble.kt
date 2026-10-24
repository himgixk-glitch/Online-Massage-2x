package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.MessageEntity
import com.example.data.model.MessageType
import com.example.ui.theme.BubbleIncoming
import com.example.ui.theme.BubbleOutgoingEnd
import com.example.ui.theme.BubbleOutgoingStart
import com.example.ui.theme.ChatterBlue
import com.example.ui.theme.ChatterCyan
import com.example.ui.theme.ChatterPink
import com.example.ui.theme.ChatterPurple
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatBubble(
    message: MessageEntity,
    isOutgoing: Boolean,
    onLongClick: () -> Unit,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val bubbleShape = if (isOutgoing) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp)
    }

    val bubbleBackground = if (isOutgoing) {
        Brush.horizontalGradient(listOf(BubbleOutgoingStart, BubbleOutgoingEnd))
    } else {
        Brush.linearGradient(listOf(BubbleIncoming, BubbleIncoming))
    }

    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(message.timestamp))

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp),
        contentAlignment = if (isOutgoing) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            horizontalAlignment = if (isOutgoing) Alignment.End else Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .widthIn(min = 70.dp, max = 290.dp)
                    .clip(bubbleShape)
                    .background(bubbleBackground)
                    .combinedClickable(
                        onClick = onClick,
                        onLongClick = onLongClick
                    )
                    .padding(
                        top = if (message.type == MessageType.IMAGE || message.type == MessageType.LOCATION) 4.dp else 8.dp,
                        bottom = 6.dp,
                        start = if (message.type == MessageType.IMAGE || message.type == MessageType.LOCATION) 4.dp else 12.dp,
                        end = if (message.type == MessageType.IMAGE || message.type == MessageType.LOCATION) 4.dp else 12.dp
                    )
                    .testTag("chat_bubble_${message.id}")
            ) {
                Column {
                    // Quoted reply message header if present
                    if (!message.replyToText.isNullOrEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isOutgoing) Color.White.copy(alpha = 0.15f)
                                    else DarkSurfaceCard
                                )
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(24.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(if (isOutgoing) Color.White else ChatterPurple)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = message.replyToText,
                                    fontSize = 11.sp,
                                    color = if (isOutgoing) Color.White.copy(alpha = 0.9f) else TextSecondary,
                                    maxLines = 1
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // Content by MessageType
                    when {
                        message.isDeleted -> {
                            Text(
                                text = "🚫 This message was deleted",
                                color = if (isOutgoing) Color.White.copy(alpha = 0.6f) else TextMuted,
                                fontSize = 13.sp,
                                fontStyle = FontStyle.Italic
                            )
                        }

                        message.type == MessageType.IMAGE -> {
                            Column {
                                AsyncImage(
                                    model = message.mediaUrl ?: "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800&auto=format&fit=crop&q=80",
                                    contentDescription = "Image message",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                                if (message.text.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = message.text,
                                        color = if (isOutgoing) Color.White else TextPrimary,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp)
                                    )
                                }
                            }
                        }

                        message.type == MessageType.VIDEO -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(DarkSurface),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = message.mediaUrl ?: "https://images.unsplash.com/photo-1536240478700-b869070f9279?w=800&auto=format&fit=crop&q=80",
                                    contentDescription = "Video thumbnail",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxWidth().height(180.dp)
                                )
                                Icon(
                                    imageVector = Icons.Default.PlayCircleOutline,
                                    contentDescription = "Play video",
                                    tint = Color.White,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }

                        message.type == MessageType.VOICE -> {
                            VoiceNotePlayer(
                                durationSeconds = if (message.voiceDurationSeconds > 0) message.voiceDurationSeconds else 12,
                                isOutgoing = isOutgoing
                            )
                        }

                        message.type == MessageType.LOCATION -> {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Brush.linearGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A)))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = "Location pin",
                                            tint = ChatterPink,
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Text(
                                            text = "Live Location Shared",
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = message.text,
                                    color = if (isOutgoing) Color.White else TextPrimary,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }
                        }

                        message.type == MessageType.FILE -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isOutgoing) Color.White.copy(alpha = 0.15f) else DarkSurfaceCard)
                                    .padding(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = "File",
                                    tint = if (isOutgoing) Color.White else ChatterCyan,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = message.text.ifEmpty { "Chatter_Document.pdf" },
                                        color = if (isOutgoing) Color.White else TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "2.4 MB • PDF Document",
                                        color = if (isOutgoing) Color.White.copy(alpha = 0.7f) else TextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        else -> {
                            Text(
                                text = message.text,
                                color = if (isOutgoing) Color.White else TextPrimary,
                                fontSize = 14.5.sp,
                                lineHeight = 19.sp
                            )
                        }
                    }

                    // Timestamp, Edited, Status ticks row
                    Row(
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (message.isEdited) {
                            Text(
                                text = "edited ",
                                color = if (isOutgoing) Color.White.copy(alpha = 0.6f) else TextMuted,
                                fontSize = 10.sp
                            )
                        }
                        Text(
                            text = formattedTime,
                            color = if (isOutgoing) Color.White.copy(alpha = 0.7f) else TextMuted,
                            fontSize = 10.sp
                        )
                        if (isOutgoing && !message.isDeleted) {
                            Spacer(modifier = Modifier.width(4.dp))
                            MessageStatusIcon(status = message.status)
                        }
                    }
                }
            }

            // Reaction pill if reacted
            if (!message.reactionEmoji.isNullOrEmpty()) {
                Box(modifier = Modifier.offset(y = (-6).dp, x = if (isOutgoing) (-4).dp else 4.dp)) {
                    ReactionBadge(emoji = message.reactionEmoji)
                }
            }
        }
    }
}
