package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.MessageStatus
import com.example.ui.theme.ChatterBlue
import com.example.ui.theme.ChatterCyan
import com.example.ui.theme.ChatterGreen
import com.example.ui.theme.ChatterPink
import com.example.ui.theme.ChatterPurple
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GradientPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(if (enabled) GradientPrimary else Brush.linearGradient(listOf(DarkSurfaceVariant, DarkSurfaceCard)))
            .testTag("gradient_button")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            leadingIcon?.let {
                it()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = if (enabled) Color.White else TextMuted,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun StoryRingAvatar(
    photoUrl: String,
    size: Dp = 56.dp,
    hasStory: Boolean = false,
    allViewed: Boolean = false,
    isOnline: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val ringBorder = when {
        hasStory && !allViewed -> Brush.sweepGradient(
            listOf(ChatterPurple, ChatterPink, ChatterBlue, ChatterCyan, ChatterPurple)
        )
        hasStory && allViewed -> Brush.linearGradient(
            listOf(TextMuted.copy(alpha = 0.5f), TextMuted.copy(alpha = 0.3f))
        )
        else -> null
    }

    Box(
        modifier = Modifier
            .size(size)
            .then(
                if (onClick != null) Modifier.clickable { onClick() } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        // Outer glow/border ring
        if (ringBorder != null) {
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(ringBorder)
                    .padding(2.5.dp)
                    .clip(CircleShape)
                    .background(DarkSurface)
            )
        }

        val avatarInnerSize = if (ringBorder != null) size - 7.dp else size

        AsyncImage(
            model = photoUrl.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&auto=format&fit=crop&q=80" },
            contentDescription = "Avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(avatarInnerSize)
                .clip(CircleShape)
                .background(DarkSurfaceVariant)
        )

        // Online dot
        if (isOnline) {
            Box(
                modifier = Modifier
                    .size(13.dp)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(DarkSurface)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(ChatterGreen)
            )
        }
    }
}

@Composable
fun MessageStatusIcon(status: MessageStatus, modifier: Modifier = Modifier) {
    when (status) {
        MessageStatus.SENT -> {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Sent",
                tint = TextMuted,
                modifier = modifier.size(15.dp)
            )
        }
        MessageStatus.DELIVERED -> {
            Icon(
                imageVector = Icons.Default.DoneAll,
                contentDescription = "Delivered",
                tint = TextSecondary,
                modifier = modifier.size(16.dp)
            )
        }
        MessageStatus.READ -> {
            Icon(
                imageVector = Icons.Default.DoneAll,
                contentDescription = "Read",
                tint = ChatterCyan,
                modifier = modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun VoiceNotePlayer(
    durationSeconds: Int,
    isOutgoing: Boolean,
    modifier: Modifier = Modifier
) {
    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "audio_bars")
    val barScale by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "waveform_anim"
    )

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            val totalSteps = durationSeconds * 10
            for (i in 0..totalSteps) {
                progress = i.toFloat() / totalSteps
                delay(100)
            }
            isPlaying = false
            progress = 0f
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .testTag("voice_note_player")
    ) {
        IconButton(
            onClick = { isPlaying = !isPlaying },
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(if (isOutgoing) Color.White.copy(alpha = 0.25f) else ChatterPurple.copy(alpha = 0.2f))
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = if (isOutgoing) Color.White else ChatterPurple,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Visual waveform bars
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            val sampleHeights = listOf(8, 14, 22, 12, 18, 26, 15, 20, 24, 16, 22, 10, 18, 24, 14)
            sampleHeights.forEachIndexed { index, baseHeight ->
                val barProgress = index.toFloat() / sampleHeights.size
                val isPlayed = barProgress <= progress
                val activeHeight = if (isPlaying) {
                    (baseHeight * (0.6f + (if (index % 2 == 0) barScale else 1f - barScale) * 0.4f)).dp
                } else {
                    baseHeight.dp
                }

                Box(
                    modifier = Modifier
                        .padding(horizontal = 1.5.dp)
                        .width(3.dp)
                        .height(activeHeight)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            when {
                                isPlayed -> if (isOutgoing) Color.White else ChatterPurple
                                else -> if (isOutgoing) Color.White.copy(alpha = 0.35f) else TextMuted.copy(alpha = 0.4f)
                            }
                        )
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = if (isPlaying) {
                val currentSec = (progress * durationSeconds).toInt()
                String.format("%02d:%02d", currentSec / 60, currentSec % 60)
            } else {
                String.format("%02d:%02d", durationSeconds / 60, durationSeconds % 60)
            },
            color = if (isOutgoing) Color.White.copy(alpha = 0.85f) else TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun EncryptionNoticeCard(modifier: Modifier = Modifier) {
    Surface(
        color = DarkSurfaceCard.copy(alpha = 0.8f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E2B48)),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(ChatterPurple.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Lock",
                    tint = ChatterPurple,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Messages and calls are end-to-end encrypted. No one outside this chat can read or listen to them.",
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                textAlign = TextAlign.Start
            )
        }
    }
}

@Composable
fun ReactionBadge(emoji: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceCard)
            .border(1.dp, DarkSurfaceVariant, RoundedCornerShape(12.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text = emoji, fontSize = 13.sp)
    }
}
