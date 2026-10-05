package com.example.ui.screens.practice

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Replay
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.OneUICard
import com.example.ui.components.OneUIHeader
import com.example.ui.theme.OneUIError
import com.example.ui.theme.OneUISuccess
import com.example.ui.viewmodel.MainViewModel

@Composable
fun MatchPairsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val leftItems by viewModel.matchLeftItems.collectAsStateWithLifecycle()
    val rightItems by viewModel.matchRightItems.collectAsStateWithLifecycle()
    val selectedLeftId by viewModel.matchSelectedLeftId.collectAsStateWithLifecycle()
    val selectedRightId by viewModel.matchSelectedRightId.collectAsStateWithLifecycle()
    val matchedIds by viewModel.matchMatchedIds.collectAsStateWithLifecycle()
    val isWrongAttempt by viewModel.matchWrongAttempt.collectAsStateWithLifecycle()

    val haptic = LocalHapticFeedback.current

    LaunchedEffect(Unit) {
        if (leftItems.isEmpty()) {
            viewModel.startMatchPairsGame()
        }
    }

    val isAllMatched = leftItems.isNotEmpty() && matchedIds.size == leftItems.size

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Viewing Area Header
        OneUIHeader(
            title = "Знайди пару",
            subtitle = "Поєднайте фразеологізми з їхніми значеннями",
            badgeText = "${matchedIds.size} / ${leftItems.size} пар",
            navigationIcon = {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Назад",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        )

        // Interaction Area
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(bottom = 90.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                if (isAllMatched) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = OneUISuccess.copy(alpha = 0.15f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = OneUISuccess,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Чудово! Всі 4 пари знайдено!",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = OneUISuccess,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Text(
                    text = "ФРАЗЕОЛОГІЗМИ (Оберіть ліворуч)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 6.dp, bottom = 8.dp)
                )

                // Left items (Idioms)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    leftItems.forEach { idiom ->
                        val isMatched = idiom.id in matchedIds
                        val isSelected = selectedLeftId == idiom.id

                        val bgColor by animateColorAsState(
                            targetValue = when {
                                isMatched -> OneUISuccess.copy(alpha = 0.2f)
                                isWrongAttempt && isSelected -> OneUIError.copy(alpha = 0.2f)
                                isSelected -> MaterialTheme.colorScheme.primaryContainer
                                else -> MaterialTheme.colorScheme.surface
                            },
                            label = "left_bg"
                        )

                        val borderColor by animateColorAsState(
                            targetValue = when {
                                isMatched -> OneUISuccess
                                isWrongAttempt && isSelected -> OneUIError
                                isSelected -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                            },
                            label = "left_border"
                        )

                        OneUICard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("match_left_${idiom.id}"),
                            onClick = if (!isMatched) {
                                {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.selectMatchLeft(idiom.id)
                                }
                            } else null,
                            cornerSize = 18.dp,
                            containerColor = bgColor,
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = idiom.phrase,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isMatched) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = "Знайдено",
                                        tint = OneUISuccess,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "ТЛУМАЧЕННЯ (Оберіть праворуч)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(start = 6.dp, bottom = 8.dp)
                )

                // Right items (Meanings)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    rightItems.forEach { (id, meaning) ->
                        val isMatched = id in matchedIds
                        val isSelected = selectedRightId == id

                        val bgColor by animateColorAsState(
                            targetValue = when {
                                isMatched -> OneUISuccess.copy(alpha = 0.2f)
                                isWrongAttempt && isSelected -> OneUIError.copy(alpha = 0.2f)
                                isSelected -> MaterialTheme.colorScheme.primaryContainer
                                else -> MaterialTheme.colorScheme.surface
                            },
                            label = "right_bg"
                        )

                        val borderColor by animateColorAsState(
                            targetValue = when {
                                isMatched -> OneUISuccess
                                isWrongAttempt && isSelected -> OneUIError
                                isSelected -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                            },
                            label = "right_border"
                        )

                        OneUICard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("match_right_$id"),
                            onClick = if (!isMatched) {
                                {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.selectMatchRight(id)
                                }
                            } else null,
                            cornerSize = 18.dp,
                            containerColor = bgColor,
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = meaning,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isMatched) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = "Знайдено",
                                        tint = OneUISuccess,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Restart / Next round button
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.startMatchPairsGame()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("match_next_round_button"),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.Replay,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isAllMatched) "Наступний раунд" else "Перемішати знову",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
