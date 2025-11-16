package com.example.itrainer.ui.distribution

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.itrainer.data.database.ITrainerDatabase
import com.example.itrainer.data.entities.Player
import com.example.itrainer.data.models.DistributionModel
import com.example.itrainer.data.models.PlayerModel
import com.example.itrainer.data.models.SubstitutionModel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class DistributionDetailsViewModel(
    application: Application,
    private val distributionId: Int
) : AndroidViewModel(application) {

    private val database = ITrainerDatabase.getDatabase(application)
    private val json = Json { ignoreUnknownKeys = true }

    data class DistributionDetails(
        val gameDate: String,
        val opponent: String,
        val players: List<Player>,
        val distribution: Map<Int, List<PlayerModel>>,
        val substitutions: Map<String, SubstitutionModel> = emptyMap() // NUEVO
    )

    private val _distributionDetails = MutableLiveData<DistributionDetails>()
    val distributionDetails: LiveData<DistributionDetails> = _distributionDetails

    init {
        loadDistribution()
    }

    private fun loadDistribution() {
        viewModelScope.launch {
            val distribution = database.gameDistributionDao().getDistributionById(distributionId)
            distribution?.let {
                val distributionModel = json.decodeFromString<DistributionModel>(it.distributionJson)
                val players = database.playerDao().getPlayersByTeam(it.teamId)

                _distributionDetails.value = DistributionDetails(
                    gameDate = it.gameDate,
                    opponent = it.opponent,
                    players = players,
                    distribution = distributionModel.periods,
                    substitutions = distributionModel.substitutions // NUEVO: Cargar las sustituciones
                )
            }
        }
    }

    class Factory(
        private val application: Application,
        private val distributionId: Int
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DistributionDetailsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DistributionDetailsViewModel(application, distributionId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
