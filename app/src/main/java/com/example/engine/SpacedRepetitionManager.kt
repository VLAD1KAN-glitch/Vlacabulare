package com.example.engine

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.AccentItem
import com.example.data.model.IdiomItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Random

/**
 * Item mastery record for Spaced Repetition.
 */
data class ItemSRSData(
    val id: Int,
    val isIdiom: Boolean,
    val masteryLevel: Int = 0,         // 0 to 5
    val consecutiveCorrect: Int = 0,   // resets on error, >= 3 marks as mastered
    val errorCount: Int = 0,           // total mistakes
    val lastReviewedTimestamp: Long = 0L
) {
    val isWeakSpot: Boolean get() = errorCount > 0 && masteryLevel < 3
    val isMastered: Boolean get() = consecutiveCorrect >= 3 || masteryLevel >= 4
}

/**
 * Smart Spaced Repetition Engine (SRS) with adaptive priority queue.
 *
 * Implements weighted priority queue:
 * - Error on accent/idiom -> resets mastery to 0, increments errorCount, marked as "Слабке місце" (HIGH priority).
 * - 3 consecutive correct -> marked as "Вивчено" (Low priority, infrequent maintenance intervals).
 * - Dynamically computes sampling weights for quiz questions and training sessions.
 */
class SpacedRepetitionManager private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("vlaculary_srs_prefs", Context.MODE_PRIVATE)
    private val random = Random()

    private val _srsState = MutableStateFlow<Map<String, ItemSRSData>>(emptyMap())
    val srsState: StateFlow<Map<String, ItemSRSData>> = _srsState.asStateFlow()

    init {
        loadFromPrefs()
    }

    private fun itemKey(id: Int, isIdiom: Boolean): String = if (isIdiom) "i_$id" else "a_$id"

    private fun loadFromPrefs() {
        val allEntries = prefs.all
        val map = mutableMapOf<String, ItemSRSData>()
        for ((key, value) in allEntries) {
            if (value is String) {
                // serialized as: "masteryLevel,consecutiveCorrect,errorCount,lastReviewed"
                val parts = value.split(",")
                if (parts.size >= 4) {
                    val isIdiom = key.startsWith("i_")
                    val id = key.substring(2).toIntOrNull() ?: continue
                    map[key] = ItemSRSData(
                        id = id,
                        isIdiom = isIdiom,
                        masteryLevel = parts[0].toIntOrNull() ?: 0,
                        consecutiveCorrect = parts[1].toIntOrNull() ?: 0,
                        errorCount = parts[2].toIntOrNull() ?: 0,
                        lastReviewedTimestamp = parts[3].toLongOrNull() ?: 0L
                    )
                }
            }
        }
        _srsState.value = map
    }

    private fun saveItem(data: ItemSRSData) {
        val key = itemKey(data.id, data.isIdiom)
        val serialized = "${data.masteryLevel},${data.consecutiveCorrect},${data.errorCount},${data.lastReviewedTimestamp}"
        prefs.edit().putString(key, serialized).apply()
    }

    /**
     * Records a review attempt.
     * @param id Identifier of item
     * @param isIdiom Whether this item is an Idiom
     * @param isCorrect True if user answered correctly
     */
    fun recordResult(id: Int, isIdiom: Boolean, isCorrect: Boolean) {
        val key = itemKey(id, isIdiom)
        val current = _srsState.value[key] ?: ItemSRSData(id = id, isIdiom = isIdiom)

        val updated = if (isCorrect) {
            val newConsecutive = current.consecutiveCorrect + 1
            val newMastery = (current.masteryLevel + 1).coerceAtMost(5)
            current.copy(
                masteryLevel = newMastery,
                consecutiveCorrect = newConsecutive,
                lastReviewedTimestamp = System.currentTimeMillis()
            )
        } else {
            // Error penalty: reset consecutive & mastery, flag as weak spot
            current.copy(
                masteryLevel = 0,
                consecutiveCorrect = 0,
                errorCount = current.errorCount + 1,
                lastReviewedTimestamp = System.currentTimeMillis()
            )
        }

        saveItem(updated)
        val newMap = _srsState.value.toMutableMap()
        newMap[key] = updated
        _srsState.value = newMap
    }

    /**
     * Calculates priority weight for weighted random selection.
     * Higher weight = appears more often.
     */
    fun getWeight(id: Int, isIdiom: Boolean): Double {
        val key = itemKey(id, isIdiom)
        val data = _srsState.value[key] ?: return 10.0 // unreviewed items have high initial weight

        if (data.isWeakSpot) {
            // Weak spots get highest priority
            return 30.0 + (data.errorCount * 5.0)
        }

        return when (data.masteryLevel) {
            0 -> 15.0
            1 -> 12.0
            2 -> 8.0
            3 -> 4.0
            4 -> 2.0
            5 -> 1.0 // mastered items appear rarely for maintenance
            else -> 5.0
        }
    }

    /**
     * Returns whether an item is currently a "Weak Spot".
     */
    fun isWeakSpot(id: Int, isIdiom: Boolean): Boolean {
        val key = itemKey(id, isIdiom)
        return _srsState.value[key]?.isWeakSpot == true
    }

    /**
     * Returns whether an item is "Mastered".
     */
    fun isMastered(id: Int, isIdiom: Boolean): Boolean {
        val key = itemKey(id, isIdiom)
        return _srsState.value[key]?.isMastered == true
    }

    /**
     * Filters list of items to only those needing review / weak spots.
     */
    fun filterWeakSpots(accents: List<AccentItem>): List<AccentItem> {
        val weakAccents = accents.filter { isWeakSpot(it.id, false) }
        return if (weakAccents.isNotEmpty()) weakAccents else accents.filter { !isMastered(it.id, false) }
    }

    fun filterWeakSpotIdioms(idioms: List<IdiomItem>): List<IdiomItem> {
        val weakIdioms = idioms.filter { isWeakSpot(it.id, true) }
        return if (weakIdioms.isNotEmpty()) weakIdioms else idioms.filter { !isMastered(it.id, true) }
    }

    /**
     * Samples items using weighted priority distribution.
     */
    fun <T> selectWeighted(items: List<T>, getId: (T) -> Int, isIdiom: Boolean, count: Int): List<T> {
        if (items.size <= count) return items.shuffled()

        val mutablePool = items.toMutableList()
        val selected = mutableListOf<T>()

        repeat(count) {
            if (mutablePool.isEmpty()) return@repeat
            val weights = mutablePool.map { getWeight(getId(it), isIdiom) }
            val totalWeight = weights.sum()

            var r = random.nextDouble() * totalWeight
            var chosenIndex = 0
            for (i in weights.indices) {
                r -= weights[i]
                if (r <= 0.0) {
                    chosenIndex = i
                    break
                }
            }

            selected.add(mutablePool.removeAt(chosenIndex))
        }

        return selected
    }

    companion object {
        @Volatile
        private var instance: SpacedRepetitionManager? = null

        fun getInstance(context: Context): SpacedRepetitionManager {
            return instance ?: synchronized(this) {
                instance ?: SpacedRepetitionManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
