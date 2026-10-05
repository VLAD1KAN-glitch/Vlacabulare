package com.example.ui.util

import android.content.Context
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback

class VlacularyHapticManager(
    private val context: Context,
    private val composeHaptics: HapticFeedback
) {
    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    /**
     * Light tick/clock pulse for card flips, button presses, segment toggles.
     */
    fun tick() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && vibrator?.hasVibrator() == true) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                composeHaptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        } catch (_: Exception) {
            composeHaptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    /**
     * Success feedback: soft double pulse confirming correct answer.
     */
    fun success() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && vibrator?.hasVibrator() == true) {
                // Two soft gentle pulses: [delay=0, vibrate=25ms, sleep=40ms, vibrate=35ms]
                val timings = longArrayOf(0, 25, 40, 35)
                val amplitudes = intArrayOf(0, 160, 0, 220)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                composeHaptics.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        } catch (_: Exception) {
            composeHaptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    /**
     * Reject/Error feedback: sharp, short buzz for incorrect accent or idiom.
     */
    fun error() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && vibrator?.hasVibrator() == true) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && vibrator?.hasVibrator() == true) {
                vibrator?.vibrate(VibrationEffect.createOneShot(70, 255))
            } else {
                composeHaptics.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        } catch (_: Exception) {
            composeHaptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
}

@Composable
fun rememberVlacularyHaptics(): VlacularyHapticManager {
    val context = LocalContext.current
    val composeHaptics = LocalHapticFeedback.current
    return remember(context, composeHaptics) {
        VlacularyHapticManager(context, composeHaptics)
    }
}
