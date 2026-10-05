package com.example.data.model

data class AccentItem(
    val id: Int,
    val wordRaw: String,               // lowercase, e.g. "агрономія"
    val displayFormatted: String,      // with uppercase stressed letters, e.g. "агронОмія"
    val stressedIndices: List<Int>,    // indices of stressed characters in wordRaw
    val isDual: Boolean = false,       // e.g. "алфАвІт", "веснЯнИй"
    val contextNote: String? = null,   // e.g. "зручність" vs "користь"
    val isFavorite: Boolean = false,
    val isLearned: Boolean = false,
    val practiceCount: Int = 0
)

data class IdiomItem(
    val id: Int,
    val phrase: String,                // e.g. "авгієві стайні"
    val meaning: String,               // e.g. "брудне місце"
    val isFavorite: Boolean = false,
    val isLearned: Boolean = false,
    val practiceCount: Int = 0
)
