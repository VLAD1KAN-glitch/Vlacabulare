package com.example.ui.screens.dictionary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AccentItem
import com.example.data.model.IdiomItem
import com.example.ui.animation.bouncyClickable
import com.example.ui.components.AccentHighlightedText
import com.example.ui.components.OneUICard
import com.example.ui.components.OneUIHeader
import com.example.ui.components.OneUISearchBar
import com.example.ui.theme.OneUIError
import com.example.ui.theme.OneUISuccess
import com.example.ui.util.VlacularyA11y
import com.example.ui.util.rememberVlacularyHaptics
import com.example.ui.viewmodel.DictionaryFilter
import com.example.ui.viewmodel.MainViewModel

@Composable
fun DictionaryScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filter by viewModel.dictionaryFilter.collectAsStateWithLifecycle()
    val filteredAccents by viewModel.filteredAccents.collectAsStateWithLifecycle()
    val filteredIdioms by viewModel.filteredIdioms.collectAsStateWithLifecycle()
    val allAccents by viewModel.accents.collectAsStateWithLifecycle()
    val allIdioms by viewModel.idioms.collectAsStateWithLifecycle()
    val overallProgress by viewModel.overallProgress.collectAsStateWithLifecycle()

    val haptics = rememberVlacularyHaptics()
    val context = LocalContext.current

    // Container Transform Detail Dialog state
    var selectedAccentDetail by remember { mutableStateOf<AccentItem?>(null) }
    var selectedIdiomDetail by remember { mutableStateOf<IdiomItem?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // One UI 9 Viewing Area Header (Top 1/3 feeling)
        OneUIHeader(
            title = "Vlaculary",
            subtitle = "Українські наголоси та фразеологізми",
            badgeText = "$overallProgress% вивчено"
        )

        // One UI Interaction Area (Bottom 2/3, thumb friendly)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            OneUISearchBar(
                query = query,
                onQueryChange = { viewModel.searchQuery.value = it },
                placeholder = "Пошук слів або фразеологізмів..."
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DictionaryFilterChip(
                    text = "Всі",
                    isSelected = filter == DictionaryFilter.ALL,
                    onClick = {
                        haptics.tick()
                        viewModel.dictionaryFilter.value = DictionaryFilter.ALL
                    }
                )
                DictionaryFilterChip(
                    text = "🔥 Слабкі місця",
                    isSelected = filter == DictionaryFilter.WEAK_SPOTS,
                    onClick = {
                        haptics.tick()
                        viewModel.dictionaryFilter.value = DictionaryFilter.WEAK_SPOTS
                    }
                )
                DictionaryFilterChip(
                    text = "Наголоси (${allAccents.size})",
                    isSelected = filter == DictionaryFilter.ACCENTS,
                    onClick = {
                        haptics.tick()
                        viewModel.dictionaryFilter.value = DictionaryFilter.ACCENTS
                    }
                )
                DictionaryFilterChip(
                    text = "Фразеологізми (${allIdioms.size})",
                    isSelected = filter == DictionaryFilter.IDIOMS,
                    onClick = {
                        haptics.tick()
                        viewModel.dictionaryFilter.value = DictionaryFilter.IDIOMS
                    }
                )
                DictionaryFilterChip(
                    text = "Вибране",
                    isSelected = filter == DictionaryFilter.FAVORITES,
                    onClick = {
                        haptics.tick()
                        viewModel.dictionaryFilter.value = DictionaryFilter.FAVORITES
                    }
                )
                DictionaryFilterChip(
                    text = "Не засвоєні",
                    isSelected = filter == DictionaryFilter.UNLEARNED,
                    onClick = {
                        haptics.tick()
                        viewModel.dictionaryFilter.value = DictionaryFilter.UNLEARNED
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Content List
        if (filteredAccents.isEmpty() && filteredIdioms.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SearchOff,
                        contentDescription = "Нічого не знайдено",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (filter == DictionaryFilter.WEAK_SPOTS) "Немає слабких місць! Всі помилки виправлено!" else "Нічого не знайдено за вашим запитом",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 100.dp)
            ) {
                if (filteredAccents.isNotEmpty() && filter != DictionaryFilter.IDIOMS) {
                    item {
                        Text(
                            text = "НАГОЛОСИ (${filteredAccents.size})",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 6.dp, top = 6.dp, bottom = 2.dp)
                        )
                    }
                    items(filteredAccents, key = { "acc_${it.id}" }) { item ->
                        val isWeak = viewModel.srsManager.isWeakSpot(item.id, false)
                        AccentCard(
                            item = item,
                            isWeakSpot = isWeak,
                            onCardClick = {
                                haptics.tick()
                                VlacularyA11y.playSubtleClick(context)
                                selectedAccentDetail = item
                            },
                            onToggleFavorite = {
                                haptics.tick()
                                viewModel.toggleAccentFavorite(item)
                            },
                            onToggleLearned = {
                                haptics.success()
                                viewModel.toggleAccentLearned(item)
                            }
                        )
                    }
                }

                if (filteredIdioms.isNotEmpty() && filter != DictionaryFilter.ACCENTS) {
                    item {
                        Text(
                            text = "ФРАЗЕОЛОГІЗМИ (${filteredIdioms.size})",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 6.dp, top = 14.dp, bottom = 2.dp)
                        )
                    }
                    items(filteredIdioms, key = { "idm_${it.id}" }) { item ->
                        val isWeak = viewModel.srsManager.isWeakSpot(item.id, true)
                        IdiomCard(
                            item = item,
                            isWeakSpot = isWeak,
                            onCardClick = {
                                haptics.tick()
                                VlacularyA11y.playSubtleClick(context)
                                selectedIdiomDetail = item
                            },
                            onToggleFavorite = {
                                haptics.tick()
                                viewModel.toggleIdiomFavorite(item)
                            },
                            onToggleLearned = {
                                haptics.success()
                                viewModel.toggleIdiomLearned(item)
                            }
                        )
                    }
                }
            }
        }
    }

    // Shared Element Container Transform Modal Preview Dialog
    selectedAccentDetail?.let { accent ->
        DetailAccentDialog(
            item = accent,
            isWeak = viewModel.srsManager.isWeakSpot(accent.id, false),
            onDismiss = { selectedAccentDetail = null },
            onToggleLearned = {
                viewModel.toggleAccentLearned(accent)
            }
        )
    }

    selectedIdiomDetail?.let { idiom ->
        DetailIdiomDialog(
            item = idiom,
            isWeak = viewModel.srsManager.isWeakSpot(idiom.id, true),
            onDismiss = { selectedIdiomDetail = null },
            onToggleLearned = {
                viewModel.toggleIdiomLearned(idiom)
            }
        )
    }
}

@Composable
private fun DictionaryFilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = text,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 13.sp
            )
        },
        shape = RoundedCornerShape(20.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = null
    )
}

@Composable
fun AccentCard(
    item: AccentItem,
    isWeakSpot: Boolean,
    onCardClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleLearned: () -> Unit,
    modifier: Modifier = Modifier
) {
    val phoneticDescription = VlacularyA11y.toPhoneticStress(item.displayFormatted)

    OneUICard(
        modifier = modifier
            .testTag("accent_card_${item.id}")
            .semantics { contentDescription = phoneticDescription },
        cornerSize = 28.dp,
        onClick = onCardClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AccentHighlightedText(
                        formattedText = item.displayFormatted,
                        fontSize = 20.sp
                    )
                    if (isWeakSpot) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = OneUIError.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Слабке місце",
                                color = OneUIError,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                val meaningCue = com.example.data.local.InitialData.getMeaningCue(item.displayFormatted)
                if (meaningCue != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                    ) {
                        Text(
                            text = "💡 Значення: $meaningCue",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                if (item.contextNote != null || item.isDual) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = item.contextNote ?: "Подвійний наголос",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onToggleLearned,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (item.isLearned) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                        contentDescription = if (item.isLearned) "Засвоєно" else "Позначити як засвоєне",
                        tint = if (item.isLearned) OneUISuccess else MaterialTheme.colorScheme.outline
                    )
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (item.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Вибране",
                        tint = if (item.isFavorite) OneUIError else MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
fun IdiomCard(
    item: IdiomItem,
    isWeakSpot: Boolean,
    onCardClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleLearned: () -> Unit,
    modifier: Modifier = Modifier
) {
    OneUICard(
        modifier = modifier.testTag("idiom_card_${item.id}"),
        cornerSize = 28.dp,
        onClick = onCardClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.phrase,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isWeakSpot) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = OneUIError.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Слабке місце",
                                color = OneUIError,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "— ${item.meaning}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onToggleLearned,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (item.isLearned) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                        contentDescription = if (item.isLearned) "Засвоєно" else "Позначити як засвоєне",
                        tint = if (item.isLearned) OneUISuccess else MaterialTheme.colorScheme.outline
                    )
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (item.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Вибране",
                        tint = if (item.isFavorite) OneUIError else MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

/**
 * Animated Container Transform Dialog previewing the full interactive card.
 */
@Composable
private fun DetailAccentDialog(
    item: AccentItem,
    isWeak: Boolean,
    onDismiss: () -> Unit,
    onToggleLearned: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
            shadowElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = if (isWeak) "СЛАБКЕ МІСЦЕ 🔥" else "НАГОЛОС",
                            color = if (isWeak) OneUIError else MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                        Icon(imageVector = Icons.Rounded.Close, contentDescription = "Закрити")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                AccentHighlightedText(
                    formattedText = item.displayFormatted,
                    fontSize = 32.sp
                )

                val homographComp = com.example.data.local.InitialData.getHomographComparison(item.wordRaw)
                if (homographComp != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "💡 Значення від наголосу:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = homographComp,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else if (item.contextNote != null || item.isDual) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = item.contextNote ?: "Подвійний наголос",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(26.dp))

                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = if (item.isLearned) OneUISuccess else MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .bouncyClickable(shape = RoundedCornerShape(24.dp)) {
                            onToggleLearned()
                            onDismiss()
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (item.isLearned) "Засвоєно ✓" else "Позначити як засвоєне",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailIdiomDialog(
    item: IdiomItem,
    isWeak: Boolean,
    onDismiss: () -> Unit,
    onToggleLearned: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
            shadowElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = if (isWeak) "СЛАБКЕ МІСЦЕ 🔥" else "ФРАЗЕОЛОГІЗМ",
                            color = if (isWeak) OneUIError else MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                        Icon(imageVector = Icons.Rounded.Close, contentDescription = "Закрити")
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "«${item.phrase}»",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = item.meaning,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(26.dp))

                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = if (item.isLearned) OneUISuccess else MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .bouncyClickable(shape = RoundedCornerShape(24.dp)) {
                            onToggleLearned()
                            onDismiss()
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (item.isLearned) "Засвоєно ✓" else "Позначити як засвоєне",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
