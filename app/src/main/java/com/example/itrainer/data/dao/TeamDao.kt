// app/src/main/java/com/example/itrainer/data/dao/TeamDao.kt
package com.example.itrainer.data.dao

import androidx.room.*
import com.example.itrainer.data.entities.Team

@Dao
interface TeamDao {
    @Query("SELECT * FROM teams WHERE categoryId = :categoryId")
    suspend fun getTeamsByCategory(categoryId: Int): List<Team>

    @Query("SELECT * FROM teams WHERE id = :teamId")
    suspend fun getTeamById(teamId: Int): Team?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeam(team: Team): Long

    @Update
    suspend fun updateTeam(team: Team)

    @Delete
    suspend fun deleteTeam(team: Team)
}


