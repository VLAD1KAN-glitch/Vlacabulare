package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.AccentItem
import com.example.data.model.IdiomItem
import com.example.data.repository.VlacularyRepository
import com.example.engine.SpacedRepetitionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class DictionaryFilter {
    ALL, ACCENTS, IDIOMS, FAVORITES, WEAK_SPOTS, UNLEARNED
}

enum class PracticeMode {
    HUB, ACCENT_TAPPER, QUIZ, MATCH_PAIRS
}

enum class LearningItemType {
    ACCENTS, IDIOMS
}

data class QuizQuestion(
    val title: String,
    val subtitle: String? = null,
    val options: List<String>,
    val correctOptionIndices: List<Int>,
    val contextHint: String? = null,
    val homographExplanation: String? = null,
    val targetMeaning: String? = null,
    val originalAccentItem: AccentItem? = null,
    val originalIdiomItem: IdiomItem? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val repository = VlacularyRepository(database.accentDao(), database.idiomDao())
    val srsManager: SpacedRepetitionManager = SpacedRepetitionManager.getInstance(application)

    val accents: StateFlow<List<AccentItem>> = repository.allAccents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val idioms: StateFlow<List<IdiomItem>> = repository.allIdioms
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val overallProgress: StateFlow<Int> = combine(accents, idioms) { aList, iList ->
        val total = aList.size + iList.size
        if (total == 0) 0
        else {
            val learned = aList.count { it.isLearned } + iList.count { it.isLearned }
            ((learned.toFloat() / total) * 100).toInt()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Search and Filter State
    val searchQuery = MutableStateFlow("")
    val dictionaryFilter = MutableStateFlow(DictionaryFilter.ALL)

    // Celebration Confetti state
    val showCelebrationConfetti = MutableStateFlow(false)

    // Container Transform / Shared Element transition: selected item for full detail drill
    val selectedDetailItem = MutableStateFlow<Any?>(null)

    // Filtered lists for dictionary
    val filteredAccents: StateFlow<List<AccentItem>> = combine(
        accents,
        searchQuery,
        dictionaryFilter,
        srsManager.srsState
    ) { list, query, filter, _ ->
        if (filter == DictionaryFilter.IDIOMS) return@combine emptyList<AccentItem>()

        list.filter { item ->
            val matchesQuery = query.isBlank() ||
                    item.wordRaw.contains(query, ignoreCase = true) ||
                    item.displayFormatted.contains(query, ignoreCase = true) ||
                    (item.contextNote != null && item.contextNote.contains(query, ignoreCase = true))

            val matchesFilter = when (filter) {
                DictionaryFilter.FAVORITES -> item.isFavorite
                DictionaryFilter.WEAK_SPOTS -> srsManager.isWeakSpot(item.id, false) || !item.isLearned
                DictionaryFilter.UNLEARNED -> !item.isLearned
                else -> true
            }

            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredIdioms: StateFlow<List<IdiomItem>> = combine(
        idioms,
        searchQuery,
        dictionaryFilter,
        srsManager.srsState
    ) { list, query, filter, _ ->
        if (filter == DictionaryFilter.ACCENTS) return@combine emptyList<IdiomItem>()

        list.filter { item ->
            val matchesQuery = query.isBlank() ||
                    item.phrase.contains(query, ignoreCase = true) ||
                    item.meaning.contains(query, ignoreCase = true)

            val matchesFilter = when (filter) {
                DictionaryFilter.FAVORITES -> item.isFavorite
                DictionaryFilter.WEAK_SPOTS -> srsManager.isWeakSpot(item.id, true) || !item.isLearned
                DictionaryFilter.UNLEARNED -> !item.isLearned
                else -> true
            }

            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Flashcards State
    val flashcardType = MutableStateFlow(LearningItemType.ACCENTS)
    val flashcardIndex = MutableStateFlow(0)
    val isCardFlipped = MutableStateFlow(false)

    fun setFlashcardType(type: LearningItemType) {
        flashcardType.value = type
        flashcardIndex.value = 0
        isCardFlipped.value = false
    }

    fun flipCard() {
        isCardFlipped.value = !isCardFlipped.value
    }

    fun nextFlashcard(learned: Boolean) {
        val total = if (flashcardType.value == LearningItemType.ACCENTS) accents.value.size else idioms.value.size
        if (total == 0) return

        val currentIndex = flashcardIndex.value
        val nextIdx = (currentIndex + 1) % total

        // Record Spaced Repetition result
        if (flashcardType.value == LearningItemType.ACCENTS) {
            val item = accents.value.getOrNull(currentIndex % accents.value.size)
            item?.let {
                srsManager.recordResult(it.id, false, isCorrect = learned)
                viewModelScope.launch { repository.setAccentLearned(it.id, learned) }
            }
        } else {
            val item = idioms.value.getOrNull(currentIndex % idioms.value.size)
            item?.let {
                srsManager.recordResult(it.id, true, isCorrect = learned)
                viewModelScope.launch { repository.setIdiomLearned(it.id, learned) }
            }
        }

        if (currentIndex + 1 >= total && learned) {
            showCelebrationConfetti.value = true
        }

        flashcardIndex.value = nextIdx
        isCardFlipped.value = false
    }

    fun previousFlashcard() {
        val total = if (flashcardType.value == LearningItemType.ACCENTS) accents.value.size else idioms.value.size
        if (total == 0) return
        val current = flashcardIndex.value
        flashcardIndex.value = if (current - 1 < 0) total - 1 else current - 1
        isCardFlipped.value = false
    }

    // Practice Hub State
    val practiceMode = MutableStateFlow(PracticeMode.HUB)

    // Non-repeating test tracking
    private val askedAccentIds = mutableSetOf<Int>()
    private val askedIdiomIds = mutableSetOf<Int>()
    private var lastAskedWordRaw: String? = null
    private val tapperDeck = mutableListOf<Int>()

    fun selectPracticeMode(mode: PracticeMode) {
        practiceMode.value = mode
        if (mode == PracticeMode.QUIZ) {
            quizScore.value = 0
            quizTotalAnswered.value = 0
            askedAccentIds.clear()
            askedIdiomIds.clear()
            lastAskedWordRaw = null
            loadNextQuizQuestion()
        } else if (mode == PracticeMode.ACCENT_TAPPER) {
            tapperScore.value = 0
            tapperDeck.clear()
            resetTapperCurrentWord()
            nextTapperWord()
        } else if (mode == PracticeMode.MATCH_PAIRS) {
            startMatchPairsGame()
        }
    }

    fun exitPracticeMode() {
        practiceMode.value = PracticeMode.HUB
    }

    // Accent Tapper State
    val tapperWordIndex = MutableStateFlow(0)
    val tapperCurrentWord = MutableStateFlow<AccentItem?>(null)
    val tapperTappedIndex = MutableStateFlow<Int?>(null)
    val tapperIsSuccess = MutableStateFlow<Boolean?>(null)
    val tapperScore = MutableStateFlow(0)

    private fun resetTapperCurrentWord() {
        tapperTappedIndex.value = null
        tapperIsSuccess.value = null
    }

    fun tapLetter(index: Int, word: AccentItem? = null) {
        if (tapperIsSuccess.value != null) return // already answered
        val currentWord = word ?: tapperCurrentWord.value ?: accents.value.firstOrNull() ?: return
        if (tapperCurrentWord.value == null) {
            tapperCurrentWord.value = currentWord
        }

        tapperTappedIndex.value = index
        val targetIndices = if (currentWord.stressedIndices.isNotEmpty()) {
            currentWord.stressedIndices
        } else {
            currentWord.displayFormatted.indices.filter { currentWord.displayFormatted[it].isUpperCase() }
        }
        val isCorrect = index in targetIndices
        tapperIsSuccess.value = isCorrect

        srsManager.recordResult(currentWord.id, isIdiom = false, isCorrect = isCorrect)

        if (isCorrect) {
            tapperScore.value += 1
            viewModelScope.launch {
                repository.setAccentLearned(currentWord.id, true)
            }
        }
    }

    fun nextTapperWord() {
        resetTapperCurrentWord()
        val list = accents.value
        if (list.isEmpty()) return

        if (tapperDeck.isEmpty()) {
            tapperDeck.addAll(list.map { it.id }.shuffled())
        }
        val nextId = tapperDeck.removeFirstOrNull()
        val nextWord = list.firstOrNull { it.id == nextId } ?: list.random()
        tapperCurrentWord.value = nextWord
    }

    // Quiz State with Adaptive Weighted Priority Queue and Zero Repetitions
    val quizType = MutableStateFlow(LearningItemType.ACCENTS)
    val currentQuizQuestion = MutableStateFlow<QuizQuestion?>(null)
    val quizSelectedOption = MutableStateFlow<Int?>(null)
    val quizIsAnswered = MutableStateFlow(false)
    val quizScore = MutableStateFlow(0)
    val quizTotalAnswered = MutableStateFlow(0)

    init {
        viewModelScope.launch {
            repository.ensureDataSeeded()
        }
        viewModelScope.launch {
            accents.collect { list ->
                if (list.isNotEmpty()) {
                    if (tapperCurrentWord.value == null) {
                        nextTapperWord()
                    }
                    if (currentQuizQuestion.value == null) {
                        loadNextQuizQuestion()
                    }
                }
            }
        }
    }

    fun setQuizType(type: LearningItemType) {
        quizType.value = type
        quizScore.value = 0
        quizTotalAnswered.value = 0
        askedAccentIds.clear()
        askedIdiomIds.clear()
        lastAskedWordRaw = null
        loadNextQuizQuestion()
    }

    fun loadNextQuizQuestion() {
        quizSelectedOption.value = null
        quizIsAnswered.value = false

        if (quizType.value == LearningItemType.ACCENTS) {
            val list = accents.value
            if (list.isEmpty()) return

            // Ensure words do not repeat during test session
            var available = list.filter { it.id !in askedAccentIds }
            if (available.isEmpty()) {
                askedAccentIds.clear()
                available = list
            }

            // Also avoid immediate back-to-back testing of same wordRaw (e.g. вигода, відомість)
            val candidates = if (available.size > 1 && lastAskedWordRaw != null) {
                val nonConsecutive = available.filter { it.wordRaw != lastAskedWordRaw }
                if (nonConsecutive.isNotEmpty()) nonConsecutive else available
            } else {
                available
            }

            // Sample target word weighted by mistake history / weak spots
            val target = srsManager.selectWeighted(candidates, { it.id }, isIdiom = false, count = 1).firstOrNull() ?: candidates.random()
            askedAccentIds.add(target.id)
            lastAskedWordRaw = target.wordRaw

            val vowels = listOf('а', 'е', 'є', 'и', 'і', 'ї', 'о', 'у', 'ю', 'я')
            val raw = target.wordRaw

            // Generate options exclusively by placing stress on different vowels of THIS word
            // No unrelated words will ever be injected into the options!
            val vowelPositions = raw.indices.filter { raw[it].lowercaseChar() in vowels }
            val generated = mutableListOf<String>()

            // Add correct form
            val correctOption = target.displayFormatted
            generated.add(correctOption)

            // Generate distractors with uppercase at other vowel positions in this exact word
            for (idx in vowelPositions) {
                val distractor = buildString {
                    for (i in raw.indices) {
                        if (i == idx) append(raw[i].uppercaseChar())
                        else append(raw[i].lowercaseChar())
                    }
                }
                if (distractor !in generated && distractor != target.displayFormatted) {
                    generated.add(distractor)
                }
            }

            val shuffledOptions = generated.shuffled()
            val correctIndices = shuffledOptions.indices.filter {
                shuffledOptions[it] == target.displayFormatted ||
                (target.isDual && target.stressedIndices.any { sIdx ->
                    sIdx < shuffledOptions[it].length && shuffledOptions[it][sIdx].isUpperCase()
                })
            }

            // Look up meaning cue and homograph comparison to provide full clarity
            val homographComparison = com.example.data.local.InitialData.getHomographComparison(target.wordRaw)
            val meaningCue = com.example.data.local.InitialData.getMeaningCue(target.displayFormatted)

            val (questionTitle, questionSubtitle) = when {
                meaningCue != null -> {
                    "Оберіть наголос для значення:" to "«$meaningCue»"
                }
                target.isDual -> {
                    "Оберіть правильний наголос:" to "«${target.wordRaw}» (подвійний наголос)"
                }
                else -> {
                    "Оберіть правильний наголос:" to "«${target.wordRaw}»"
                }
            }

            val explanation = when {
                homographComparison != null -> homographComparison
                target.contextNote != null -> target.contextNote
                else -> null
            }

            currentQuizQuestion.value = QuizQuestion(
                title = questionTitle,
                subtitle = questionSubtitle,
                options = shuffledOptions,
                correctOptionIndices = if (correctIndices.isNotEmpty()) correctIndices else listOf(shuffledOptions.indexOf(target.displayFormatted).coerceAtLeast(0)),
                contextHint = target.contextNote,
                homographExplanation = explanation,
                targetMeaning = meaningCue,
                originalAccentItem = target
            )
        } else {
            // Idioms Quiz: Non-repeating in current session
            val list = idioms.value
            if (list.size < 4) return

            var available = list.filter { it.id !in askedIdiomIds }
            if (available.isEmpty()) {
                askedIdiomIds.clear()
                available = list
            }

            val target = srsManager.selectWeighted(available, { it.id }, isIdiom = true, count = 1).firstOrNull() ?: available.random()
            askedIdiomIds.add(target.id)

            val wrongMeanings = list.filter { it.id != target.id }.shuffled().take(3).map { it.meaning }
            val allOptions = (wrongMeanings + target.meaning).shuffled()

            currentQuizQuestion.value = QuizQuestion(
                title = "Що означає фразеологізм:",
                subtitle = "«${target.phrase}»",
                options = allOptions,
                correctOptionIndices = listOf(allOptions.indexOf(target.meaning)),
                contextHint = null,
                homographExplanation = null,
                targetMeaning = null,
                originalIdiomItem = target
            )
        }
    }

    fun answerQuiz(optionIndex: Int) {
        if (quizIsAnswered.value) return
        quizSelectedOption.value = optionIndex
        quizIsAnswered.value = true
        quizTotalAnswered.value += 1

        val question = currentQuizQuestion.value ?: return
        val isCorrect = optionIndex in question.correctOptionIndices

        // Update Spaced Repetition adaptive priority weights
        if (question.originalAccentItem != null) {
            srsManager.recordResult(question.originalAccentItem.id, isIdiom = false, isCorrect = isCorrect)
        } else if (question.originalIdiomItem != null) {
            srsManager.recordResult(question.originalIdiomItem.id, isIdiom = true, isCorrect = isCorrect)
        }

        if (isCorrect) {
            quizScore.value += 1
            viewModelScope.launch {
                question.originalAccentItem?.let { repository.setAccentLearned(it.id, true) }
                question.originalIdiomItem?.let { repository.setIdiomLearned(it.id, true) }
            }
        }

        // Trigger confetti celebration on achieving 10 answers with good score
        if (quizTotalAnswered.value % 10 == 0 && quizScore.value >= 7) {
            showCelebrationConfetti.value = true
        }
    }

    // Match Pairs Game State (4 idioms vs 4 meanings)
    val matchLeftItems = MutableStateFlow<List<IdiomItem>>(emptyList())
    val matchRightItems = MutableStateFlow<List<Pair<Int, String>>>(emptyList())
    val matchSelectedLeftId = MutableStateFlow<Int?>(null)
    val matchSelectedRightId = MutableStateFlow<Int?>(null)
    val matchMatchedIds = MutableStateFlow<Set<Int>>(emptySet())
    val matchWrongAttempt = MutableStateFlow(false)

    fun startMatchPairsGame() {
        val list = idioms.value
        if (list.size < 4) return
        val selected = list.shuffled().take(4)
        matchLeftItems.value = selected
        matchRightItems.value = selected.map { it.id to it.meaning }.shuffled()
        matchSelectedLeftId.value = null
        matchSelectedRightId.value = null
        matchMatchedIds.value = emptySet()
        matchWrongAttempt.value = false
    }

    fun selectMatchLeft(id: Int) {
        if (id in matchMatchedIds.value) return
        matchSelectedLeftId.value = id
        checkMatch()
    }

    fun selectMatchRight(id: Int) {
        if (id in matchMatchedIds.value) return
        matchSelectedRightId.value = id
        checkMatch()
    }

    private fun checkMatch() {
        val left = matchSelectedLeftId.value
        val right = matchSelectedRightId.value
        if (left != null && right != null) {
            if (left == right) {
                // Match!
                matchMatchedIds.value = matchMatchedIds.value + left
                matchSelectedLeftId.value = null
                matchSelectedRightId.value = null
                matchWrongAttempt.value = false
                viewModelScope.launch {
                    repository.setIdiomLearned(left, true)
                }
                if (matchMatchedIds.value.size >= 4) {
                    showCelebrationConfetti.value = true
                }
            } else {
                // Wrong match
                matchWrongAttempt.value = true
                viewModelScope.launch {
                    kotlinx.coroutines.delay(700)
                    matchSelectedLeftId.value = null
                    matchSelectedRightId.value = null
                    matchWrongAttempt.value = false
                }
            }
        }
    }

    // Toggle Favorites
    fun toggleAccentFavorite(item: AccentItem) {
        viewModelScope.launch {
            repository.toggleAccentFavorite(item.id, item.isFavorite)
        }
    }

    fun toggleIdiomFavorite(item: IdiomItem) {
        viewModelScope.launch {
            repository.toggleIdiomFavorite(item.id, item.isFavorite)
        }
    }

    fun toggleAccentLearned(item: AccentItem) {
        viewModelScope.launch {
            repository.setAccentLearned(item.id, !item.isLearned)
        }
    }

    fun toggleIdiomLearned(item: IdiomItem) {
        viewModelScope.launch {
            repository.setIdiomLearned(item.id, !item.isLearned)
        }
    }

    fun resetAllProgress() {
        viewModelScope.launch {
            repository.resetAllProgress()
            quizScore.value = 0
            quizTotalAnswered.value = 0
            tapperScore.value = 0
        }
    }
}
