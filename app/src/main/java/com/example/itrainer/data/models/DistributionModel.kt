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
    val opponent: String,
    val substitutions: Map<String, SubstitutionModel> = emptyMap() // Nueva: información de sustituciones
)

@Serializable
data class PlayerModel(
    val id: Int,
    val name: String,
    val number: Int
)

@Serializable
data class SubstitutionModel(
    val playerId: Int,
    val period: Int,
    val isOut: Boolean = false,      // true si sale (X roja)
    val isSubstitute: Boolean = false // true si es cambio (verde)
)
