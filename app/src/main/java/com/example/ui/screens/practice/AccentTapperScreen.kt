package com.example.ui.screens.practice

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AccentItem
import com.example.ui.animation.bouncyClickable
import com.example.ui.animation.shakeEffect
import com.example.ui.components.OneUICard
import com.example.ui.components.OneUIHeader
import com.example.ui.theme.OneUIError
import com.example.ui.theme.OneUISuccess
import com.example.ui.theme.VlacularyTheme
import com.example.ui.util.rememberVlacularyHaptics
import com.example.ui.viewmodel.MainViewModel

@Composable
fun AccentTapperScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accents by viewModel.accents.collectAsStateWithLifecycle()
    val tapperWord by viewModel.tapperCurrentWord.collectAsStateWithLifecycle()
    val tappedIndex by viewModel.tapperTappedIndex.collectAsStateWithLifecycle()
    val isSuccess by viewModel.tapperIsSuccess.collectAsStateWithLifecycle()
    val score by viewModel.tapperScore.collectAsStateWithLifecycle()

    LaunchedEffect(accents) {
        if (tapperWord == null && accents.isNotEmpty()) {
            viewModel.nextTapperWord()
        }
    }

    if (accents.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Завантаження слів...")
        }
        return
    }

    val currentWord = tapperWord ?: accents.first()

    AccentTapperContent(
        currentWord = currentWord,
        score = score,
        tappedIndex = tappedIndex,
        isSuccess = isSuccess,
        onTapLetter = { charIndex ->
            viewModel.tapLetter(charIndex, currentWord)
        },
        onNextWord = {
            viewModel.nextTapperWord()
        },
        onBack = onBack,
        modifier = modifier
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AccentTapperContent(
    currentWord: AccentItem,
    score: Int,
    tappedIndex: Int?,
    isSuccess: Boolean?,
    onTapLetter: (Int) -> Unit,
    onNextWord: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = rememberVlacularyHaptics()
    var shakeCounter by remember(currentWord.id) { mutableIntStateOf(0) }
    var consonantNotice by remember(currentWord.id) { mutableStateOf(false) }

    val rawLetters = currentWord.wordRaw.toList()
    val ukrainianVowels = remember {
        setOf('а', 'е', 'є', 'и', 'і', 'ї', 'о', 'у', 'ю', 'я', 'А', 'Е', 'Є', 'И', 'І', 'Ї', 'О', 'У', 'Ю', 'Я')
    }

    val tileWidth = when {
        rawLetters.size > 14 -> 34.dp
        rawLetters.size > 9 -> 40.dp
        else -> 48.dp
    }
    val tileHeight = when {
        rawLetters.size > 14 -> 42.dp
        rawLetters.size > 9 -> 48.dp
        else -> 52.dp
    }
    val tileFontSize = when {
        rawLetters.size > 14 -> 18.sp
        rawLetters.size > 9 -> 21.sp
        else -> 24.sp
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // One UI 9 Viewing Area Header
        OneUIHeader(
            title = "Вкажи наголос",
            subtitle = "Тапніть по голосній літері, на яку падає наголос",
            badgeText = "Рахунок: $score",
            navigationIcon = {
                IconButton(
                    onClick = {
                        haptics.tick()
                        onBack()
                    },
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

        // One UI 9 Interaction Area: Pinned Bottom Button + Scrollable Card Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Interactive Word Board with Squircle 30dp
                OneUICard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .shakeEffect(shakeTrigger = shakeCounter),
                    cornerSize = 30.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val meaningCue = com.example.data.local.InitialData.getMeaningCue(currentWord.displayFormatted)
                        if (meaningCue != null) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                                modifier = Modifier.padding(bottom = 12.dp)
                            ) {
                                Text(
                                    text = "Значення: «$meaningCue»",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Text(
                            text = "ТАПНІТЬ ПО ГОЛОСНІЙ",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Letter Tiles Grid / Row with Spring Physics
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rawLetters.forEachIndexed { index, char ->
                                if (char == ' ' || char == '(' || char == ')') {
                                    Text(
                                        text = char.toString(),
                                        fontSize = 22.sp,
                                        modifier = Modifier.padding(horizontal = 2.dp, vertical = 8.dp)
                                    )
                                } else {
                                    val isVowel = char in ukrainianVowels
                                    val isTapped = tappedIndex == index
                                    val isCorrectStressed = index in currentWord.stressedIndices ||
                                            (currentWord.displayFormatted.getOrNull(index)?.isUpperCase() == true)
                                    val isAnswered = isSuccess != null

                                    // Pulse animation for the correct letter
                                    val pulseScale by animateFloatAsState(
                                        targetValue = if (isAnswered && isCorrectStressed) 1.12f else 1f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessMedium
                                        ),
                                        label = "letter_pulse"
                                    )

                                    val tileBgColor by animateColorAsState(
                                        targetValue = when {
                                            isAnswered && isCorrectStressed -> OneUISuccess.copy(alpha = 0.22f)
                                            isAnswered && isTapped && !isCorrectStressed -> OneUIError.copy(alpha = 0.22f)
                                            isVowel -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        },
                                        label = "tile_bg"
                                    )

                                    val tileBorderColor by animateColorAsState(
                                        targetValue = when {
                                            isAnswered && isCorrectStressed -> OneUISuccess
                                            isAnswered && isTapped && !isCorrectStressed -> OneUIError
                                            isVowel -> MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                        },
                                        label = "tile_border"
                                    )

                                    val tileTextColor by animateColorAsState(
                                        targetValue = when {
                                            isAnswered && isCorrectStressed -> OneUISuccess
                                            isAnswered && isTapped && !isCorrectStressed -> OneUIError
                                            isVowel -> MaterialTheme.colorScheme.primary
                                            else -> MaterialTheme.colorScheme.onSurface
                                        },
                                        label = "tile_text"
                                    )

                                    Surface(
                                        onClick = {
                                            if (isAnswered) return@Surface
                                            if (!isVowel) {
                                                haptics.tick()
                                                shakeCounter++
                                                consonantNotice = true
                                                return@Surface
                                            }
                                            consonantNotice = false
                                            val willBeCorrect = isCorrectStressed
                                            if (willBeCorrect) {
                                                haptics.success()
                                            } else {
                                                haptics.error()
                                                shakeCounter++
                                            }
                                            onTapLetter(index)
                                        },
                                        enabled = !isAnswered,
                                        shape = RoundedCornerShape(16.dp),
                                        color = tileBgColor,
                                        border = BorderStroke(if (isVowel) 1.5.dp else 1.dp, tileBorderColor),
                                        modifier = Modifier
                                            .padding(horizontal = 2.dp)
                                            .size(width = tileWidth, height = tileHeight)
                                            .graphicsLayer {
                                                scaleX = pulseScale
                                                scaleY = pulseScale
                                            }
                                            .testTag("tapper_letter_$index")
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = char.toString(),
                                                fontSize = tileFontSize,
                                                fontWeight = FontWeight.Bold,
                                                color = tileTextColor
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (consonantNotice && isSuccess == null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "💡 Наголос падає тільки на голосні літери!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Animated feedback status banner
                        if (isSuccess != null) {
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = if (isSuccess) OneUISuccess.copy(alpha = 0.15f) else OneUIError.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, if (isSuccess) OneUISuccess.copy(alpha = 0.4f) else OneUIError.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = if (isSuccess) Icons.Rounded.Check else Icons.Rounded.Close,
                                        contentDescription = null,
                                        tint = if (isSuccess) OneUISuccess else OneUIError,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = if (isSuccess) "Правильно! Молодець!" else "Помилка! Правильно: ${currentWord.displayFormatted}",
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSuccess) OneUISuccess else OneUIError,
                                        fontSize = 15.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            val homographComp = com.example.data.local.InitialData.getHomographComparison(currentWord.wordRaw)
                            if (homographComp != null) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "💡 Значення від наголосу:",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = homographComp,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            } else if (currentWord.contextNote != null || currentWord.isDual) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = currentWord.contextNote ?: "💡 Подвійний наголос",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom Next Word Button: Always Pinned and Visible
            Button(
                onClick = {
                    haptics.tick()
                    onNextWord()
                },
                enabled = isSuccess != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("tapper_next_button"),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Наступне слово",
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

@Preview(name = "Light Mode", showBackground = true)
@Composable
fun AccentTapperScreenPreviewLight() {
    VlacularyTheme(darkTheme = false) {
        AccentTapperContent(
            currentWord = AccentItem(
                id = 1,
                wordRaw = "агрономія",
                displayFormatted = "агронОмія",
                stressedIndices = listOf(5)
            ),
            score = 7,
            tappedIndex = 5,
            isSuccess = true,
            onTapLetter = {},
            onNextWord = {},
            onBack = {}
        )
    }
}

@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun AccentTapperScreenPreviewDark() {
    VlacularyTheme(darkTheme = true) {
        AccentTapperContent(
            currentWord = AccentItem(
                id = 7,
                wordRaw = "бешкет",
                displayFormatted = "бЕшкет",
                stressedIndices = listOf(1)
            ),
            score = 12,
            tappedIndex = 3,
            isSuccess = false,
            onTapLetter = {},
            onNextWord = {},
            onBack = {}
        )
    }
}
