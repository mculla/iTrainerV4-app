// app/src/main/java/com/example/itrainer/data/dao/PlayerStatsDao.kt
package com.example.itrainer.data.dao

import androidx.room.*
import com.example.itrainer.data.entities.PlayerStats

@Dao
interface PlayerStatsDao {
    @Query("SELECT * FROM player_stats WHERE playerId = :playerId AND seasonYear = :seasonYear")
    suspend fun getPlayerStats(playerId: Int, seasonYear: Int): PlayerStats?

    @Query("SELECT * FROM player_stats WHERE playerId = :playerId ORDER BY seasonYear DESC")
    suspend fun getAllPlayerStats(playerId: Int): List<PlayerStats>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStats(stats: PlayerStats)

    @Update
    suspend fun updateStats(stats: PlayerStats)

    @Query("SELECT * FROM player_stats WHERE seasonYear = :seasonYear ORDER BY totalPeriods DESC")
    suspend fun getSeasonLeaderboard(seasonYear: Int): List<PlayerStats>
}