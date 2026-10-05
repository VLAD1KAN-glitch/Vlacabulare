package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.animation.bouncyClickable
import com.example.ui.util.rememberVlacularyHaptics

enum class AppNavTab(
    val label: String,
    val icon: ImageVector,
    val testTag: String
) {
    DICTIONARY("Словник", Icons.AutoMirrored.Rounded.MenuBook, "nav_tab_dictionary"),
    FLASHCARDS("Картки", Icons.Rounded.Style, "nav_tab_flashcards"),
    PRACTICE("Тренажери", Icons.Rounded.Extension, "nav_tab_practice"),
    PROGRESS("Прогрес", Icons.Rounded.BarChart, "nav_tab_progress")
}

/**
 * Samsung One UI 9 Floating Pill Navigation Bar.
 * Enhanced with Dynamic Glassmorphism Blur, Spring bounce, and tactile Haptics.
 */
@Composable
fun OneUIPillNav(
    selectedTab: AppNavTab,
    onTabSelected: (AppNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = rememberVlacularyHaptics()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
            shadowElevation = 8.dp,
            tonalElevation = 4.dp,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
            ),
            modifier = Modifier
                .height(64.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppNavTab.entries.forEach { tab ->
                    val isSelected = tab == selectedTab
                    val iconColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "tab_icon_color"
                    )
                    val bgColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f) else Color.Transparent,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "tab_bg_color"
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(bgColor)
                            .bouncyClickable(
                                shape = RoundedCornerShape(24.dp),
                                targetScaleOnPress = 0.94f
                            ) {
                                haptics.tick()
                                onTabSelected(tab)
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag(tab.testTag),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                tint = iconColor,
                                modifier = Modifier.size(22.dp)
                            )
                            if (isSelected) {
                                Text(
                                    text = tab.label,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
