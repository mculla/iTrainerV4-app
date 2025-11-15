package com.example.itrainer.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val periodsCount: Int,
    val playersPerPeriod: Int,
    val minPeriodsPerPlayer: Int,
    val maxPeriodsPerPlayer: Int,
    val minPlayers: Int,
    val maxPlayers: Int
)