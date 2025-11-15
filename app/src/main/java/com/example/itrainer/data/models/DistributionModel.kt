// app/src/main/java/com/example/itrainer/data/models/DistributionModel.kt
package com.example.itrainer.data.models

import kotlinx.serialization.Serializable
import java.util.Date
import com.example.itrainer.data.entities.GameDistribution
import com.example.itrainer.data.entities.Team

@Serializable
data class DistributionModel(
    val periods: Map<Int, List<PlayerModel>>,
    val date: Long,
    val gameDate: String,
    val opponent: String
)

@Serializable
data class PlayerModel(
    val id: Int,
    val name: String,
    val number: Int
)

