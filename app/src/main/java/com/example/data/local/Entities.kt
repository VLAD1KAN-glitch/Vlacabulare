package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.AccentItem
import com.example.data.model.IdiomItem

@Entity(tableName = "accent_words")
data class AccentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val wordRaw: String,
    val displayFormatted: String,
    val stressedIndicesString: String, // comma-separated, e.g. "5" or "3,5"
    val isDual: Boolean = false,
    val contextNote: String? = null,
    val isFavorite: Boolean = false,
    val isLearned: Boolean = false,
    val practiceCount: Int = 0
) {
    fun toModel(): AccentItem {
        val parsed = stressedIndicesString.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
        val uppercaseIndices = displayFormatted.indices.filter { displayFormatted[it].isUpperCase() }
        val finalIndices = if (parsed.isNotEmpty()) parsed else uppercaseIndices
        return AccentItem(
            id = id,
            wordRaw = wordRaw,
            displayFormatted = displayFormatted,
            stressedIndices = finalIndices,
            isDual = isDual,
            contextNote = contextNote,
            isFavorite = isFavorite,
            isLearned = isLearned,
            practiceCount = practiceCount
        )
    }
}

@Entity(tableName = "idioms")
data class IdiomEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val phrase: String,
    val meaning: String,
    val isFavorite: Boolean = false,
    val isLearned: Boolean = false,
    val practiceCount: Int = 0
) {
    fun toModel(): IdiomItem = IdiomItem(
        id = id,
        phrase = phrase,
        meaning = meaning,
        isFavorite = isFavorite,
        isLearned = isLearned,
        practiceCount = practiceCount
    )
}
