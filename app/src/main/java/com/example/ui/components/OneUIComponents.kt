package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.animation.bouncyClickable
import com.example.ui.theme.AccentVowelOrange

/**
 * Samsung One UI 9 Viewing Area Header.
 *
 * Embodies the core One UI design philosophy:
 * - Upper third of the screen is dedicated to the "Viewing Area" (lots of air, clear hierarchy).
 * - Leaves the lower two-thirds of the screen as the ergonomic "Interaction Area" reachable by thumb.
 * - Bold display typography (32-34sp) with optional badge pill and descriptive subtitle.
 *
 * @param title Primary screen headline
 * @param modifier Layout modifier
 * @param subtitle Optional secondary description text
 * @param badgeText Optional pill badge (e.g. progress percentage, counter)
 * @param navigationIcon Optional leading action (e.g. back button)
 * @param actions Optional trailing actions row (e.g. filters, settings)
 * @param expandedViewingArea When true, adds generous One UI ergonomic vertical breathing space
 */
@Composable
fun OneUIHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    badgeText: String? = null,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: (@Composable () -> Unit)? = null,
    expandedViewingArea: Boolean = false
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(
                top = if (expandedViewingArea) 28.dp else 16.dp,
                bottom = if (expandedViewingArea) 28.dp else 18.dp
            )
    ) {
        // Top action bar (Navigation & contextual actions)
        if (navigationIcon != null || actions != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.minimumInteractiveComponentSize()) {
                    navigationIcon?.invoke()
                }
                Box(modifier = Modifier.minimumInteractiveComponentSize()) {
                    actions?.invoke()
                }
            }
        }

        // Title and optional status badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                fontSize = 36.sp,
                modifier = Modifier.testTag("one_ui_header_title")
            )

            if (badgeText != null) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Subtitle / descriptive progress text
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Samsung One UI 9 Squircle Card.
 *
 * Specifically styled with **28dp–32dp rounded corners** (`RoundedCornerShape(28.dp)`),
 * smooth squircle curvature, subtle border outline, and bouncy spring-based touch response.
 *
 * @param modifier Layout modifier
 * @param onClick Optional click listener. When provided, configures tactile spring scale-down on press.
 * @param cornerSize Corner radius, defaults to 28dp (One UI 9 Squircle).
 * @param containerColor Surface background color
 * @param contentColor Text/Icon content color
 * @param border Subtle outer stroke for soft depth
 * @param elevation Material 3 elevation specification
 * @param content Composable card body
 */
@Composable
fun OneUICard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    cornerSize: Dp = 28.dp,
    shape: Shape = RoundedCornerShape(cornerSize),
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    border: BorderStroke? = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
    elevation: CardElevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    content: @Composable () -> Unit
) {
    val cardModifier = if (onClick != null) {
        modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .bouncyClickable(shape = shape, targetScaleOnPress = 0.96f, onClick = onClick)
    } else {
        modifier
            .fillMaxWidth()
            .clip(shape)
    }

    Card(
        modifier = cardModifier,
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        border = border,
        elevation = elevation
    ) {
        content()
    }
}

/**
 * Thumb-accessible Samsung One UI 9 Search Bar.
 *
 * Placed in the lower ergonomic Interaction Area within natural thumb reach.
 * Features 26dp rounded corners (`RoundedCornerShape(26.dp)`), frosted surface variant background,
 * search icon with primary tint, animated clear button, and accessible keyboard actions.
 *
 * @param query Current search input string
 * @param onQueryChange Callback invoked when search query text changes
 * @param placeholder Hint text shown when search query is empty
 * @param modifier Layout modifier
 * @param cornerSize Corner radius, defaulting to 26dp
 * @param onClear Callback invoked when the user taps the clear 'X' button
 * @param trailingContent Optional additional trailing action (e.g. voice input or filter toggle)
 */
@Composable
fun OneUISearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Пошук слова чи фразеологізму...",
    modifier: Modifier = Modifier,
    cornerSize: Dp = 26.dp,
    onClear: () -> Unit = { onQueryChange("") },
    trailingContent: (@Composable () -> Unit)? = null
) {
    val searchShape = RoundedCornerShape(cornerSize)

    Surface(
        shape = searchShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(
                elevation = 2.dp,
                shape = searchShape,
                ambientColor = Color.Black.copy(alpha = 0.05f)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = "Пошук",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                        maxLines = 1
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("one_ui_search_input")
                )
            }

            if (trailingContent != null && query.isEmpty()) {
                trailingContent()
            }

            AnimatedVisibility(
                visible = query.isNotEmpty(),
                enter = fadeIn() + scaleIn(initialScale = 0.8f),
                exit = fadeOut() + scaleOut(targetScale = 0.8f)
            ) {
                IconButton(
                    onClick = onClear,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .testTag("search_clear_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Очистити пошук",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Text composable highlighting stressed vowels in bold vivid Accent color.
 * e.g. for "агронОмія" -> 'О' is styled with AccentVowelOrange and bold weight.
 */
@Composable
fun AccentHighlightedText(
    formattedText: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 22.sp,
    baseColor: Color = MaterialTheme.colorScheme.onSurface,
    accentColor: Color = AccentVowelOrange
) {
    val annotatedString = buildAnnotatedString {
        for (ch in formattedText) {
            if (ch.isUpperCase() && ch !in "() -'\".,/") {
                withStyle(
                    style = SpanStyle(
                        color = accentColor,
                        fontWeight = FontWeight.ExtraBold
                    )
                ) {
                    append(ch)
                }
            } else {
                withStyle(
                    style = SpanStyle(
                        color = baseColor,
                        fontWeight = FontWeight.Medium
                    )
                ) {
                    append(ch)
                }
            }
        }
    }

    Text(
        text = annotatedString,
        fontSize = fontSize,
        lineHeight = (fontSize.value + 6).sp,
        modifier = modifier
    )
}

/**
 * Samsung One UI 9 Segmented Pill Switcher.
 *
 * Provides a tactile, full-height pill selector with smooth color animation,
 * high contrast, and 26dp outer rounded corners with 22dp inner active pill.
 */
@Composable
fun <T> OneUISegmentedSwitch(
    options: List<Pair<T, String>>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 50.dp,
    cornerSize: Dp = 26.dp
) {
    Surface(
        shape = RoundedCornerShape(cornerSize),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            options.forEach { (key, label) ->
                val isSelected = key == selectedOption

                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    animationSpec = tween(durationMillis = 200),
                    label = "segmented_bg"
                )

                val textColor by animateColorAsState(
                    targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = tween(durationMillis = 200),
                    label = "segmented_text"
                )

                val interactionSource = remember { MutableInteractionSource() }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(cornerSize - 4.dp))
                        .background(bgColor)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = ripple(bounded = true)
                        ) {
                            onOptionSelected(key)
                        }
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = textColor,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

