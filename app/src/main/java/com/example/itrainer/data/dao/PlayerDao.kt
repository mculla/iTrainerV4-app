// app/src/main/java/com/example/itrainer/data/dao/PlayerDao.kt
package com.example.itrainer.data.dao

import androidx.room.*
import com.example.itrainer.data.entities.Player

@Dao
interface PlayerDao {
    @Query("SELECT * FROM players WHERE teamId = :teamId ORDER BY number")
    suspend fun getPlayersByTeam(teamId: Int): List<Player>

    @Query("SELECT * FROM players WHERE id = :playerId")
    suspend fun getPlayerById(playerId: Int): Player?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayer(player: Player): Long

    @Update
    suspend fun updatePlayer(player: Player)

    @Delete
    suspend fun deletePlayer(player: Player)

    @Query("DELETE FROM players WHERE teamId = :teamId")
    suspend fun deleteTeamPlayers(teamId: Int)
}
