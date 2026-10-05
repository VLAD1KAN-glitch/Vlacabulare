package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.InitialData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Vlaculary", appName)
    }

    @Test
    fun `verify dataset strictly matches requirement`() {
        assertEquals(26, InitialData.IDIOMS.size)
        assertTrue(InitialData.ACCENT_WORDS.size >= 34)
        assertTrue(InitialData.ACCENT_WORDS.any { it.displayFormatted == "агронОмія" })
        assertTrue(InitialData.ACCENT_WORDS.any { it.displayFormatted == "Аркушик" })
        assertTrue(InitialData.ACCENT_WORDS.any { it.wordRaw == "відвести" && it.displayFormatted == "відвестИ" && it.stressedIndicesString == "7" })
        assertTrue(InitialData.IDIOMS.any { it.phrase == "авгієві стайні" && it.meaning == "брудне місце" })
        assertTrue(InitialData.IDIOMS.any { it.phrase == "вичитати мораль" && it.meaning == "дорікати, сварити" })
    }

    @Test
    fun `verify homographs have clear meaning distinctions`() {
        val vygoda1 = InitialData.ACCENT_WORDS.first { it.displayFormatted == "вИгода" }
        val vygoda2 = InitialData.ACCENT_WORDS.first { it.displayFormatted == "вигОда" }
        assertTrue(vygoda1.contextNote?.contains("зручність") == true)
        assertTrue(vygoda2.contextNote?.contains("користь") == true)

        val vidomist1 = InitialData.ACCENT_WORDS.first { it.displayFormatted == "вІдомість" }
        val vidomist2 = InitialData.ACCENT_WORDS.first { it.displayFormatted == "відОмість" }
        assertTrue(vidomist1.contextNote?.contains("документ") == true || vidomist1.contextNote?.contains("список") == true)
        assertTrue(vidomist2.contextNote?.contains("популярність") == true || vidomist2.contextNote?.contains("слава") == true)

        assertEquals("документ, список, реєстр", InitialData.getMeaningCue("вІдомість"))
        assertEquals("популярність, слава", InitialData.getMeaningCue("відОмість"))
        assertEquals("зручність, комфорт", InitialData.getMeaningCue("вИгода"))
        assertEquals("користь, зиск", InitialData.getMeaningCue("вигОда"))
    }

    @Test
    fun `verify accent tapper functionality`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val viewModel = com.example.ui.viewmodel.MainViewModel(app)
        viewModel.selectPracticeMode(com.example.ui.viewmodel.PracticeMode.ACCENT_TAPPER)

        val word = InitialData.ACCENT_WORDS.first { it.wordRaw == "відвести" }.toModel()
        // Tap incorrect letter (e.g. index 0 'в')
        viewModel.tapLetter(0, word)
        assertEquals(false, viewModel.tapperIsSuccess.value)
        assertEquals(0, viewModel.tapperTappedIndex.value)
        assertEquals(0, viewModel.tapperScore.value)

        // Move to next word resets status
        viewModel.nextTapperWord()
        assertEquals(null, viewModel.tapperIsSuccess.value)
        assertEquals(null, viewModel.tapperTappedIndex.value)

        // Tap correct letter for word (index 7 'и')
        viewModel.tapLetter(7, word)
        assertEquals(true, viewModel.tapperIsSuccess.value)
        assertEquals(7, viewModel.tapperTappedIndex.value)
        assertEquals(1, viewModel.tapperScore.value)
    }
}
