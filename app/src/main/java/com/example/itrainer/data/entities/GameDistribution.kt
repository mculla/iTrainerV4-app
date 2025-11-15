// app/src/main/java/com/example/itrainer/data/entities/GameDistribution.kt
package com.example.itrainer.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.itrainer.data.database.Converters
import java.util.Date

@Entity(tableName = "game_distributions")
@TypeConverters(Converters::class)
data class GameDistribution(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val teamId: Int,
    val date: Date,
    val gameDate: String,
    val opponent: String,
    val distributionJson: String // Almacenará la distribución en formato JSON
)