package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AccentDao {
    @Query("SELECT * FROM accent_words ORDER BY id ASC")
    fun getAllAccents(): Flow<List<AccentEntity>>

    @Query("SELECT * FROM accent_words ORDER BY id ASC")
    suspend fun getAllAccentsList(): List<AccentEntity>

    @Query("SELECT COUNT(*) FROM accent_words")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(accents: List<AccentEntity>)

    @Update
    suspend fun update(accent: AccentEntity)

    @Query("UPDATE accent_words SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Int, isFavorite: Boolean)

    @Query("UPDATE accent_words SET isLearned = :isLearned, practiceCount = practiceCount + 1 WHERE id = :id")
    suspend fun setLearned(id: Int, isLearned: Boolean)

    @Query("UPDATE accent_words SET isLearned = 0, practiceCount = 0")
    suspend fun resetAllProgress()
}

@Dao
interface IdiomDao {
    @Query("SELECT * FROM idioms ORDER BY id ASC")
    fun getAllIdioms(): Flow<List<IdiomEntity>>

    @Query("SELECT COUNT(*) FROM idioms")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(idioms: List<IdiomEntity>)

    @Update
    suspend fun update(idiom: IdiomEntity)

    @Query("UPDATE idioms SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Int, isFavorite: Boolean)

    @Query("UPDATE idioms SET isLearned = :isLearned, practiceCount = practiceCount + 1 WHERE id = :id")
    suspend fun setLearned(id: Int, isLearned: Boolean)

    @Query("UPDATE idioms SET isLearned = 0, practiceCount = 0")
    suspend fun resetAllProgress()
}
