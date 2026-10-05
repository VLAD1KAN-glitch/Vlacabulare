package com.example.data.repository

import com.example.data.local.AccentDao
import com.example.data.local.IdiomDao
import com.example.data.local.InitialData
import com.example.data.model.AccentItem
import com.example.data.model.IdiomItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class VlacularyRepository(
    private val accentDao: AccentDao,
    private val idiomDao: IdiomDao
) {
    val allAccents: Flow<List<AccentItem>> = accentDao.getAllAccents().map { list ->
        list.map { it.toModel() }
    }

    val allIdioms: Flow<List<IdiomItem>> = idiomDao.getAllIdioms().map { list ->
        list.map { it.toModel() }
    }

    suspend fun ensureDataSeeded() = withContext(Dispatchers.IO) {
        val existing = accentDao.getAllAccentsList()
        if (existing.isEmpty()) {
            accentDao.insertAll(InitialData.ACCENT_WORDS)
        } else {
            val existingByDisplay = existing.associateBy { it.displayFormatted }
            val toInsert = mutableListOf<com.example.data.local.AccentEntity>()
            for (item in InitialData.ACCENT_WORDS) {
                val match = existingByDisplay[item.displayFormatted]
                if (match == null) {
                    toInsert.add(item.copy(id = 0))
                } else if (match.contextNote != item.contextNote ||
                    match.stressedIndicesString != item.stressedIndicesString ||
                    match.isDual != item.isDual) {
                    accentDao.update(match.copy(
                        contextNote = item.contextNote,
                        stressedIndicesString = item.stressedIndicesString,
                        isDual = item.isDual
                    ))
                }
            }
            if (toInsert.isNotEmpty()) {
                accentDao.insertAll(toInsert)
            }
        }
        if (idiomDao.getCount() == 0) {
            idiomDao.insertAll(InitialData.IDIOMS)
        }
    }

    suspend fun toggleAccentFavorite(id: Int, currentFav: Boolean) = withContext(Dispatchers.IO) {
        accentDao.setFavorite(id, !currentFav)
    }

    suspend fun setAccentLearned(id: Int, isLearned: Boolean) = withContext(Dispatchers.IO) {
        accentDao.setLearned(id, isLearned)
    }

    suspend fun toggleIdiomFavorite(id: Int, currentFav: Boolean) = withContext(Dispatchers.IO) {
        idiomDao.setFavorite(id, !currentFav)
    }

    suspend fun setIdiomLearned(id: Int, isLearned: Boolean) = withContext(Dispatchers.IO) {
        idiomDao.setLearned(id, isLearned)
    }

    suspend fun resetAllProgress() = withContext(Dispatchers.IO) {
        accentDao.resetAllProgress()
        idiomDao.resetAllProgress()
    }
}
