// app/src/main/java/com/example/itrainer/data/dao/GameDistributionDao.kt
package com.example.itrainer.data.dao

import androidx.room.*
import com.example.itrainer.data.entities.GameDistribution
import java.util.*

@Dao
interface GameDistributionDao {
    @Query("SELECT * FROM game_distributions ORDER BY date DESC")
    suspend fun getAllDistributions(): List<GameDistribution>

    @Query("SELECT * FROM game_distributions WHERE teamId = :teamId ORDER BY date DESC")
    suspend fun getDistributionsByTeam(teamId: Int): List<GameDistribution>

    @Query("SELECT * FROM game_distributions WHERE id = :distributionId")
    suspend fun getDistributionById(distributionId: Int): GameDistribution?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDistribution(distribution: GameDistribution): Long

    @Delete
    suspend fun deleteDistribution(distribution: GameDistribution)
}