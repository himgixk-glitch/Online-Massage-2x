package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val DarkBackground = Color(0xFF121212)
val DarkSurface = Color(0xFF1A1A26)
val DarkSurfaceVariant = Color(0xFF242338)
val DarkSurfaceCard = Color(0xFF1E1D2D)

val ChatterPurple = Color(0xFF7B5BFE)
val ChatterBlue = Color(0xFF5B8DEF)
val ChatterCyan = Color(0xFF38BDF8)
val ChatterPink = Color(0xFFEC4899)
val ChatterGreen = Color(0xFF10B981)
val ChatterRed = Color(0xFFEF4444)

val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)
val TextMuted = Color(0xFF64748B)

val BubbleOutgoingStart = Color(0xFF7B5BFE)
val BubbleOutgoingEnd = Color(0xFF5B8DEF)
val BubbleIncoming = Color(0xFF232236)

val GradientPrimary = Brush.horizontalGradient(
    colors = listOf(ChatterPurple, ChatterBlue)
)

val GradientStory = Brush.sweepGradient(
    colors = listOf(ChatterPurple, ChatterBlue, ChatterPink, ChatterPurple)
)

val GradientCall = Brush.verticalGradient(
    colors = listOf(Color(0xFF1F1B3B), Color(0xFF0F0E17))
)
