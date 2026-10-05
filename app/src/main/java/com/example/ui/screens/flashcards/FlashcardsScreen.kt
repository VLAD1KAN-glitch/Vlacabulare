package com.example.ui.screens.flashcards

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AccentItem
import com.example.data.model.IdiomItem
import com.example.ui.animation.bouncyClickable
import com.example.ui.components.AccentHighlightedText
import com.example.ui.components.OneUIHeader
import com.example.ui.components.OneUISegmentedSwitch
import com.example.ui.theme.OneUIError
import com.example.ui.theme.OneUISuccess
import com.example.ui.theme.VlacularyTheme
import com.example.ui.util.rememberVlacularyHaptics
import com.example.ui.viewmodel.LearningItemType
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun FlashcardsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val accents by viewModel.accents.collectAsStateWithLifecycle()
    val idioms by viewModel.idioms.collectAsStateWithLifecycle()
    val cardType by viewModel.flashcardType.collectAsStateWithLifecycle()
    val currentIndex by viewModel.flashcardIndex.collectAsStateWithLifecycle()
    val isFlipped by viewModel.isCardFlipped.collectAsStateWithLifecycle()

    val totalItems = if (cardType == LearningItemType.ACCENTS) accents.size else idioms.size
    val currentAccent = accents.getOrNull(currentIndex % accents.size.coerceAtLeast(1))
    val currentIdiom = idioms.getOrNull(currentIndex % idioms.size.coerceAtLeast(1))

    FlashcardsContent(
        cardType = cardType,
        currentIndex = currentIndex,
        totalItems = totalItems,
        isFlipped = isFlipped,
        currentAccent = currentAccent,
        currentIdiom = currentIdiom,
        accentsCount = accents.size,
        idiomsCount = idioms.size,
        onTypeChanged = { viewModel.setFlashcardType(it) },
        onFlip = { viewModel.flipCard() },
        onNextCard = { learned -> viewModel.nextFlashcard(learned) },
        onPrevCard = { viewModel.previousFlashcard() },
        modifier = modifier
    )
}

@Composable
fun FlashcardsContent(
    cardType: LearningItemType,
    currentIndex: Int,
    totalItems: Int,
    isFlipped: Boolean,
    currentAccent: AccentItem?,
    currentIdiom: IdiomItem?,
    accentsCount: Int,
    idiomsCount: Int,
    onTypeChanged: (LearningItemType) -> Unit,
    onFlip: () -> Unit,
    onNextCard: (Boolean) -> Unit,
    onPrevCard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = rememberVlacularyHaptics()
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    // Spring flip rotation (0 to 180 degrees)
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "flashcard_spring_flip"
    )

    // Swipe Offset (interactive horizontal drag physics)
    val offsetX = remember { Animatable(0f) }
    val thresholdPx = with(density) { 130.dp.toPx() }

    LaunchedEffect(currentIndex, cardType) {
        offsetX.snapTo(0f)
    }

    val dragProgress = (offsetX.value / thresholdPx).coerceIn(-1.5f, 1.5f)
    val cardTiltAngle = dragProgress * 9f // Dynamic tilt up to ~13 degrees

    val tintColor = when {
        offsetX.value > 10f -> OneUISuccess.copy(alpha = (abs(dragProgress) * 0.22f).coerceAtMost(0.35f))
        offsetX.value < -10f -> OneUIError.copy(alpha = (abs(dragProgress) * 0.22f).coerceAtMost(0.35f))
        else -> Color.Transparent
    }

    val borderColor by animateColorAsState(
        targetValue = when {
            offsetX.value > 40f -> OneUISuccess.copy(alpha = 0.8f)
            offsetX.value < -40f -> OneUIError.copy(alpha = 0.8f)
            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        },
        label = "border_color_swipe"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // One UI 9 Viewing Area Header (Spacious top third)
        OneUIHeader(
            title = "Флеш-картки",
            subtitle = "Свайп праворуч — знаю, ліворуч — повторити",
            badgeText = "${currentIndex + 1} / $totalItems"
        )

        // One UI 9 Interaction Area
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(bottom = 90.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // One UI 9 Segmented Pill Switcher
            OneUISegmentedSwitch(
                options = listOf(
                    LearningItemType.ACCENTS to "Наголоси ($accentsCount)",
                    LearningItemType.IDIOMS to "Фразеологізми ($idiomsCount)"
                ),
                selectedOption = cardType,
                onOptionSelected = { selectedType ->
                    haptics.tick()
                    onTypeChanged(selectedType)
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 3D Flippable and Swipeable Flashcard Container
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                        .graphicsLayer {
                            rotationY = rotation
                            rotationZ = cardTiltAngle
                            cameraDistance = 16f * density.density // Prevents clipping out of screen
                        }
                        .pointerInput(currentIndex, cardType) {
                            detectDragGestures(
                                onDragEnd = {
                                    coroutineScope.launch {
                                        if (offsetX.value > thresholdPx) {
                                            // Swipe right -> Mastered
                                            haptics.success()
                                            offsetX.animateTo(
                                                targetValue = thresholdPx * 3f,
                                                animationSpec = tween(180)
                                            )
                                            onNextCard(true)
                                            offsetX.snapTo(0f)
                                        } else if (offsetX.value < -thresholdPx) {
                                            // Swipe left -> Repeat
                                            haptics.error()
                                            offsetX.animateTo(
                                                targetValue = -thresholdPx * 3f,
                                                animationSpec = tween(180)
                                            )
                                            onNextCard(false)
                                            offsetX.snapTo(0f)
                                        } else {
                                            // Snap back to center with One UI bouncy spring
                                            offsetX.animateTo(
                                                targetValue = 0f,
                                                animationSpec = spring(
                                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                                    stiffness = Spring.StiffnessLow
                                                )
                                            )
                                        }
                                    }
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    coroutineScope.launch {
                                        offsetX.snapTo(offsetX.value + dragAmount.x)
                                    }
                                }
                            )
                        }
                        .bouncyClickable(
                            shape = RoundedCornerShape(30.dp),
                            targetScaleOnPress = 0.98f
                        ) {
                            haptics.tick()
                            onFlip()
                        }
                        .testTag("flashcard_flip_target"),
                    shape = RoundedCornerShape(30.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    border = BorderStroke(1.5.dp, borderColor)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(tintColor)
                            .padding(26.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Swipe visual feedback badge
                        if (offsetX.value > 40f) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = OneUISuccess,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "ЗНАЮ ✓",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        } else if (offsetX.value < -40f) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = OneUIError,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "ПОВТОРИТИ ✕",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }

                        if (rotation <= 90f) {
                            // Front of Card
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(bottom = 18.dp)
                                ) {
                                    Text(
                                        text = if (cardType == LearningItemType.ACCENTS) "СЛОВО" else "ФРАЗЕОЛОГІЗМ",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                    )
                                }

                                Text(
                                    text = if (cardType == LearningItemType.ACCENTS) {
                                        currentAccent?.wordRaw ?: ""
                                    } else {
                                        currentIdiom?.phrase ?: ""
                                    },
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                if (cardType == LearningItemType.ACCENTS && currentAccent != null) {
                                    val cue = com.example.data.local.InitialData.getMeaningCue(currentAccent.displayFormatted)
                                    if (cue != null) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                                        ) {
                                            Text(
                                                text = "У значенні: «$cue»",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.SwapHoriz,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Тапніть, щоб побачити відповідь",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        } else {
                            // Back of Card (Rotated 180 degrees back so it's readable)
                            Column(
                                modifier = Modifier.graphicsLayer { rotationY = 180f },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = OneUISuccess.copy(alpha = 0.15f),
                                    modifier = Modifier.padding(bottom = 16.dp)
                                ) {
                                    Text(
                                        text = if (cardType == LearningItemType.ACCENTS) "ПРАВИЛЬНИЙ НАГОЛОС" else "ТЛУМАЧЕННЯ",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = OneUISuccess,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                    )
                                }

                                if (cardType == LearningItemType.ACCENTS && currentAccent != null) {
                                    AccentHighlightedText(
                                        formattedText = currentAccent.displayFormatted,
                                        fontSize = 30.sp
                                    )

                                    val homographComp = com.example.data.local.InitialData.getHomographComparison(currentAccent.wordRaw)
                                    if (homographComp != null) {
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                                                Text(
                                                    text = "💡 Значення від наголосу:",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = homographComp,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    } else if (currentAccent.contextNote != null || currentAccent.isDual) {
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Text(
                                                text = currentAccent.contextNote ?: "Подвійний наголос",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                } else if (currentIdiom != null) {
                                    Text(
                                        text = "«${currentIdiom.phrase}»",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = currentIdiom.meaning,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons in Ergonomic Interaction Area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Repeat / Needs review button
                OutlinedButton(
                    onClick = {
                        haptics.error()
                        onNextCard(false)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .bouncyClickable(
                            shape = RoundedCornerShape(28.dp),
                            targetScaleOnPress = 0.95f
                        ) {
                            haptics.error()
                            onNextCard(false)
                        }
                        .testTag("flashcard_repeat_button"),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = OneUIError
                    ),
                    border = BorderStroke(1.5.dp, OneUIError.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Повторити",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Повторити",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Mastered / Know it button
                Button(
                    onClick = {
                        haptics.success()
                        onNextCard(true)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .bouncyClickable(
                            shape = RoundedCornerShape(28.dp),
                            targetScaleOnPress = 0.95f
                        ) {
                            haptics.success()
                            onNextCard(true)
                        }
                        .testTag("flashcard_mastered_button"),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OneUISuccess,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "Засвоєно",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Знаю!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Navigation Arrows
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        haptics.tick()
                        onPrevCard()
                    },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Попередня",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "Картка ${currentIndex + 1} з $totalItems",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                IconButton(
                    onClick = {
                        haptics.tick()
                        onNextCard(false)
                    },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = "Наступна",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Composable
fun FlashcardsScreenPreviewLight() {
    VlacularyTheme(darkTheme = false) {
        FlashcardsContent(
            cardType = LearningItemType.ACCENTS,
            currentIndex = 2,
            totalItems = 32,
            isFlipped = false,
            currentAccent = AccentItem(
                id = 3,
                wordRaw = "аркушик",
                displayFormatted = "Аркушик",
                stressedIndices = listOf(0)
            ),
            currentIdiom = null,
            accentsCount = 32,
            idiomsCount = 26,
            onTypeChanged = {},
            onFlip = {},
            onNextCard = {},
            onPrevCard = {}
        )
    }
}

@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun FlashcardsScreenPreviewDark() {
    VlacularyTheme(darkTheme = true) {
        FlashcardsContent(
            cardType = LearningItemType.ACCENTS,
            currentIndex = 2,
            totalItems = 32,
            isFlipped = true,
            currentAccent = AccentItem(
                id = 3,
                wordRaw = "аркушик",
                displayFormatted = "Аркушик",
                stressedIndices = listOf(0)
            ),
            currentIdiom = null,
            accentsCount = 32,
            idiomsCount = 26,
            onTypeChanged = {},
            onFlip = {},
            onNextCard = {},
            onPrevCard = {}
        )
    }
}
