package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AppNavTab
import com.example.ui.components.ConfettiCelebration
import com.example.ui.components.OneUIPillNav
import com.example.ui.screens.dictionary.DictionaryScreen
import com.example.ui.screens.flashcards.FlashcardsScreen
import com.example.ui.screens.practice.AccentTapperScreen
import com.example.ui.screens.practice.MatchPairsScreen
import com.example.ui.screens.practice.PracticeHubScreen
import com.example.ui.screens.practice.QuizScreen
import com.example.ui.screens.progress.ProgressScreen
import com.example.ui.theme.VlacularyTheme
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.PracticeMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VlacularyTheme {
                VlacularyApp()
            }
        }
    }
}

@Composable
fun VlacularyApp(
    viewModel: MainViewModel = viewModel()
) {
    var currentTab by rememberSaveable { mutableStateOf(AppNavTab.DICTIONARY) }
    var practiceMode by rememberSaveable { mutableStateOf(PracticeMode.HUB) }

    // Android System Back Navigation Handler
    BackHandler(enabled = currentTab != AppNavTab.DICTIONARY || practiceMode != PracticeMode.HUB) {
        if (currentTab == AppNavTab.PRACTICE && practiceMode != PracticeMode.HUB) {
            viewModel.exitPracticeMode()
            practiceMode = PracticeMode.HUB
        } else {
            currentTab = AppNavTab.DICTIONARY
        }
    }

    val isSubPracticeScreen = currentTab == AppNavTab.PRACTICE && practiceMode != PracticeMode.HUB
    val showConfetti by viewModel.showCelebrationConfetti.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (!isSubPracticeScreen) {
                OneUIPillNav(
                    selectedTab = currentTab,
                    onTabSelected = { tab ->
                        currentTab = tab
                        if (tab == AppNavTab.PRACTICE) {
                            practiceMode = PracticeMode.HUB
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            AnimatedContent(
                targetState = currentTab to practiceMode,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_transition"
            ) { (tab, pMode) ->
                when (tab) {
                    AppNavTab.DICTIONARY -> {
                        DictionaryScreen(viewModel = viewModel)
                    }
                    AppNavTab.FLASHCARDS -> {
                        FlashcardsScreen(viewModel = viewModel)
                    }
                    AppNavTab.PRACTICE -> {
                        when (pMode) {
                            PracticeMode.HUB -> {
                                PracticeHubScreen(
                                    onSelectMode = { selectedMode ->
                                        viewModel.selectPracticeMode(selectedMode)
                                        practiceMode = selectedMode
                                    }
                                )
                            }
                            PracticeMode.ACCENT_TAPPER -> {
                                AccentTapperScreen(
                                    viewModel = viewModel,
                                    onBack = {
                                        viewModel.exitPracticeMode()
                                        practiceMode = PracticeMode.HUB
                                    }
                                )
                            }
                            PracticeMode.QUIZ -> {
                                QuizScreen(
                                    viewModel = viewModel,
                                    onBack = {
                                        viewModel.exitPracticeMode()
                                        practiceMode = PracticeMode.HUB
                                    }
                                )
                            }
                            PracticeMode.MATCH_PAIRS -> {
                                MatchPairsScreen(
                                    viewModel = viewModel,
                                    onBack = {
                                        viewModel.exitPracticeMode()
                                        practiceMode = PracticeMode.HUB
                                    }
                                )
                            }
                        }
                    }
                    AppNavTab.PROGRESS -> {
                        ProgressScreen(viewModel = viewModel)
                    }
                }
            }

            // High-performance Canvas Confetti Celebration overlay
            ConfettiCelebration(
                trigger = showConfetti,
                onAnimationEnd = {
                    viewModel.showCelebrationConfetti.value = false
                }
            )
        }
    }
}
