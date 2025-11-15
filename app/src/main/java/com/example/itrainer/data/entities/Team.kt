// app/src/main/java/com/example/itrainer/data/entities/Team.kt
package com.example.itrainer.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "teams")
data class Team(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val categoryId: Int
)