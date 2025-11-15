package com.example.itrainer.data.models

import com.example.itrainer.data.entities.GameDistribution
import com.example.itrainer.data.entities.Team

data class GameDistributionWithTeam(
    val distribution: GameDistribution,
    val team: Team
)