package com.example.ui.util

import android.content.Context
import android.media.AudioManager
import android.view.SoundEffectConstants

object VlacularyA11y {

    /**
     * Converts formatted word with capitalized stressed vowel (e.g. "агронОмія")
     * into a standard phonetic string with Unicode Combining Acute Accent (\u0301).
     *
     * Example: "агронОмія" -> "агроно́мія"
     * This instructs TalkBack and text-to-speech synthesizers to pronounce the word
     * with the exact Ukrainian vocal stress intended.
     */
    fun toPhoneticStress(displayFormatted: String): String {
        val sb = StringBuilder()
        for (ch in displayFormatted) {
            if (ch.isUpperCase() && ch !in "() -'\".,/") {
                sb.append(ch.lowercaseChar())
                sb.append('\u0301') // Unicode COMBINING ACUTE ACCENT
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    /**
     * Plays a gentle, subtle system click sound effect synchronized with haptic feedback.
     */
    fun playSubtleClick(context: Context) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            audioManager?.playSoundEffect(SoundEffectConstants.CLICK, 0.4f)
        } catch (_: Exception) {
            // gracefully ignore if audio service is unavailable
        }
    }
}
