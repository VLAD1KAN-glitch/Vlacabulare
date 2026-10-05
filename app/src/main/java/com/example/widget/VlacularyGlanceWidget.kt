package com.example.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.MainActivity
import com.example.data.local.InitialData

class VlacularyGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefs = context.getSharedPreferences("widget_state", Context.MODE_PRIVATE)
        val currentIndex = prefs.getInt("current_idx", 0)

        val accents = InitialData.ACCENT_WORDS
        val idioms = InitialData.IDIOMS

        val isAccent = (currentIndex % 2 == 0)
        val titleText = if (isAccent) {
            val accent = accents[currentIndex % accents.size]
            accent.displayFormatted
        } else {
            val idiom = idioms[(currentIndex / 2) % idioms.size]
            "«${idiom.phrase}»"
        }

        val subText = if (isAccent) {
            val accent = accents[currentIndex % accents.size]
            accent.contextNote ?: "Наголос на виділеній літері"
        } else {
            val idiom = idioms[(currentIndex / 2) % idioms.size]
            idiom.meaning
        }

        val badgeLabel = if (isAccent) "НАГОЛОС ДНЯ" else "ФРАЗЕОЛОГІЗМ ДНЯ"

        provideContent {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .cornerRadius(32.dp)
                    .background(ColorProvider(Color(0xFF16181E)))
                    .padding(16.dp)
                    .clickable(actionStartActivity<MainActivity>()),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = GlanceModifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Header Row: App Name + Badge + Refresh Action
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = GlanceModifier
                                .cornerRadius(10.dp)
                                .background(ColorProvider(Color(0xFF4361EE)))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = badgeLabel,
                                style = TextStyle(
                                    color = ColorProvider(Color.White),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Spacer(modifier = GlanceModifier.defaultWeight())

                        // Next / Refresh button
                        Box(
                            modifier = GlanceModifier
                                .cornerRadius(16.dp)
                                .background(ColorProvider(Color(0xFF262933)))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                .clickable(actionRunCallback<NextWordActionCallback>())
                        ) {
                            Text(
                                text = "↻ Інше",
                                style = TextStyle(
                                    color = ColorProvider(Color(0xFF738AFF)),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }

                    Spacer(modifier = GlanceModifier.height(10.dp))

                    // Main display item
                    Text(
                        text = titleText,
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 2
                    )

                    Spacer(modifier = GlanceModifier.height(4.dp))

                    // Secondary note / meaning
                    Text(
                        text = subText,
                        style = TextStyle(
                            color = ColorProvider(Color(0xFF9AA0AC)),
                            fontSize = 13.sp
                        ),
                        maxLines = 2
                    )
                }
            }
        }
    }
}

class NextWordActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: androidx.glance.action.ActionParameters) {
        val prefs = context.getSharedPreferences("widget_state", Context.MODE_PRIVATE)
        val current = prefs.getInt("current_idx", 0)
        prefs.edit().putInt("current_idx", current + 1).apply()
        VlacularyGlanceWidget().update(context, glanceId)
    }
}

class VlacularyWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = VlacularyGlanceWidget()
}
