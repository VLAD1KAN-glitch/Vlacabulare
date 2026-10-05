package com.example.ui.screens.practice

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.NavigateNext
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AccentHighlightedText
import com.example.ui.components.OneUICard
import com.example.ui.components.OneUIHeader
import com.example.ui.components.OneUISegmentedSwitch
import com.example.ui.theme.OneUIError
import com.example.ui.theme.OneUISuccess
import com.example.ui.viewmodel.LearningItemType
import com.example.ui.viewmodel.MainViewModel

@Composable
fun QuizScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val quizType by viewModel.quizType.collectAsStateWithLifecycle()
    val question by viewModel.currentQuizQuestion.collectAsStateWithLifecycle()
    val selectedOption by viewModel.quizSelectedOption.collectAsStateWithLifecycle()
    val isAnswered by viewModel.quizIsAnswered.collectAsStateWithLifecycle()
    val score by viewModel.quizScore.collectAsStateWithLifecycle()
    val totalAnswered by viewModel.quizTotalAnswered.collectAsStateWithLifecycle()

    val haptic = LocalHapticFeedback.current

    LaunchedEffect(quizType) {
        if (question == null) {
            viewModel.loadNextQuizQuestion()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Viewing Area Header
        OneUIHeader(
            title = "Тести",
            subtitle = if (quizType == LearningItemType.ACCENTS) "Перевір правильність наголосу" else "Перевір знання фразеологізмів",
            badgeText = "$score / $totalAnswered",
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
                .padding(horizontal = 24.dp)
                .padding(bottom = 90.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // One UI 9 Segmented Pill Switcher (Accents vs Idioms)
                OneUISegmentedSwitch(
                    options = listOf(
                        LearningItemType.ACCENTS to "Наголоси",
                        LearningItemType.IDIOMS to "Фразеологізми"
                    ),
                    selectedOption = quizType,
                    onOptionSelected = { selectedType ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.setQuizType(selectedType)
                    }
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Question Box
                if (question != null) {
                    OneUICard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerSize = 24.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = question!!.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            if (question!!.subtitle != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = question!!.subtitle!!,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Options
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        question!!.options.forEachIndexed { index, optionText ->
                            val isChosen = selectedOption == index
                            val isCorrectAnswer = index in question!!.correctOptionIndices

                            val cardBgColor by animateColorAsState(
                                targetValue = when {
                                    isAnswered && isCorrectAnswer -> OneUISuccess.copy(alpha = 0.2f)
                                    isAnswered && isChosen && !isCorrectAnswer -> OneUIError.copy(alpha = 0.2f)
                                    isChosen -> MaterialTheme.colorScheme.primaryContainer
                                    else -> MaterialTheme.colorScheme.surface
                                },
                                label = "option_bg"
                            )

                            val borderColor by animateColorAsState(
                                targetValue = when {
                                    isAnswered && isCorrectAnswer -> OneUISuccess
                                    isAnswered && isChosen && !isCorrectAnswer -> OneUIError
                                    isChosen -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                                },
                                label = "option_border"
                            )

                            OneUICard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("quiz_option_$index"),
                                onClick = if (!isAnswered) {
                                    {
                                        val willBeCorrect = index in question!!.correctOptionIndices
                                        if (willBeCorrect) {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        } else {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        }
                                        viewModel.answerQuiz(index)
                                    }
                                } else null,
                                cornerSize = 20.dp,
                                containerColor = cardBgColor,
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 18.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    if (quizType == LearningItemType.ACCENTS) {
                                        AccentHighlightedText(
                                            formattedText = optionText,
                                            fontSize = 18.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                    } else {
                                        Text(
                                            text = optionText,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    if (isAnswered) {
                                        if (isCorrectAnswer) {
                                            Icon(
                                                imageVector = Icons.Rounded.Check,
                                                contentDescription = "Правильно",
                                                tint = OneUISuccess,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        } else if (isChosen) {
                                            Icon(
                                                imageVector = Icons.Rounded.Close,
                                                contentDescription = "Неправильно",
                                                tint = OneUIError,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Meaning and Stress Explanation Card when answered
                    if (isAnswered && (question!!.homographExplanation != null || question!!.contextHint != null)) {
                        Spacer(modifier = Modifier.height(14.dp))
                        OneUICard(
                            modifier = Modifier.fillMaxWidth(),
                            cornerSize = 18.dp,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Значення від наголосу:",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = question!!.homographExplanation ?: question!!.contextHint ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Bottom Next Question Button
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.loadNextQuizQuestion()
                },
                enabled = isAnswered,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("quiz_next_button"),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Наступне питання",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Rounded.NavigateNext,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
