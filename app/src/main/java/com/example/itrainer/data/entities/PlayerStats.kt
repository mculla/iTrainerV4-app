// app/src/main/java/com/example/itrainer/data/entities/PlayerStats.kt
package com.example.itrainer.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "player_stats",
    foreignKeys = [
        ForeignKey(
            entity = Player::class,
            parentColumns = ["id"],
            childColumns = ["playerId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PlayerStats(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val playerId: Int,
    val seasonYear: Int, // Por si quieres separar por temporadas
    val totalGames: Int = 0,
    val totalPeriods: Int = 0,
    val gamesAsStarter: Int = 0, // Partidos como titular
    val gamesAsSubstitute: Int = 0, // Partidos como suplente
    val averagePeriodsPerGame: Float = 0f
)