package com.example.ui.screens.practice

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.OneUICard
import com.example.ui.components.OneUIHeader
import com.example.ui.theme.AccentVowelOrange
import com.example.ui.viewmodel.PracticeMode

@Composable
fun PracticeHubScreen(
    onSelectMode: (PracticeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Viewing Area Header
        OneUIHeader(
            title = "Тренажери",
            subtitle = "Оберіть інтерактивний режим тренування"
        )

        // Interaction Area
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(bottom = 90.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Mode 1: Accent Tapper
            PracticeModeCard(
                title = "Вкажи наголос",
                description = "Тапайте безпосередньо по голосній літері, на яку падає наголос.",
                badge = "Інтерактив",
                icon = Icons.Rounded.TouchApp,
                accentColor = AccentVowelOrange,
                testTag = "practice_mode_tapper",
                onClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    onSelectMode(PracticeMode.ACCENT_TAPPER)
                }
            )

            // Mode 2: Quiz
            PracticeModeCard(
                title = "Тести на вибір",
                description = "4 варіанти відповідей для слів із наголосами та фразеологізмів.",
                badge = "Тести",
                icon = Icons.Rounded.Quiz,
                accentColor = MaterialTheme.colorScheme.primary,
                testTag = "practice_mode_quiz",
                onClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    onSelectMode(PracticeMode.QUIZ)
                }
            )

            // Mode 3: Match Pairs
            PracticeModeCard(
                title = "Знайди пару",
                description = "З'єднуйте фразеологізми з їхніми значеннями в реальному часі.",
                badge = "Гра",
                icon = Icons.Rounded.Shuffle,
                accentColor = MaterialTheme.colorScheme.secondary,
                testTag = "practice_mode_match",
                onClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    onSelectMode(PracticeMode.MATCH_PAIRS)
                }
            )
        }
    }
}

@Composable
private fun PracticeModeCard(
    title: String,
    description: String,
    badge: String,
    icon: ImageVector,
    accentColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    OneUICard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        onClick = onClick,
        cornerSize = 26.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = accentColor.copy(alpha = 0.12f),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = accentColor.copy(alpha = 0.15f),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
