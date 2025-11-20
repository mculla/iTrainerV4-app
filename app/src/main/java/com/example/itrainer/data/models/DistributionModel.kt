// app/src/main/java/com/example/itrainer/data/models/DistributionModel.kt
package com.example.itrainer.data.models

import kotlinx.serialization.Serializable

@Serializable
data class DistributionModel(
    val periods: Map<Int, List<PlayerModel>>,
    val date: Long,
    val gameDate: String,
    val opponent: String,
    val categoryId: Int,
    val substitutions: Map<String, SubstitutionInfoModel>? = null // Para guardar sustituciones
)

@Serializable
data class PlayerModel(
    val id: Int,
    val name: String,
    val number: Int
)

@Serializable
data class SubstitutionInfoModel( // NUEVO: Modelo serializable
    val playerId: Int,
    val isOut: Boolean = false,
    val isSubstitute: Boolean = false
)