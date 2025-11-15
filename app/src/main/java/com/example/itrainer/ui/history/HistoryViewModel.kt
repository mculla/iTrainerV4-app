// app/src/main/java/com/example/itrainer/ui/history/HistoryViewModel.kt
package com.example.itrainer.ui.history

import android.app.Application
import androidx.lifecycle.*
import com.example.itrainer.data.database.ITrainerDatabase
import com.example.itrainer.data.entities.GameDistribution
import com.example.itrainer.data.models.DistributionModel
import com.example.itrainer.data.models.GameDistributionWithTeam
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

class HistoryViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database = ITrainerDatabase.getDatabase(application)
    private val gameDistributionDao = database.gameDistributionDao()
    private val teamDao = database.teamDao()

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        encodeDefaults = true
    }

    private val _distributions = MutableLiveData<List<GameDistributionWithTeam>>()
    val distributions: LiveData<List<GameDistributionWithTeam>> = _distributions

    private val _selectedDistribution = MutableLiveData<DistributionModel?>()
    val selectedDistribution: LiveData<DistributionModel?> = _selectedDistribution

    fun loadDistributions() {
        viewModelScope.launch {
            val allDistributions = gameDistributionDao.getAllDistributions()
            val distributionsWithTeams = allDistributions.map { distribution ->
                val team = teamDao.getTeamById(distribution.teamId)
                GameDistributionWithTeam(distribution, team!!)
            }
            _distributions.value = distributionsWithTeams
        }
    }

    fun selectDistribution(distribution: GameDistribution) {
        _selectedDistribution.value = json.decodeFromString<DistributionModel>(
            distribution.distributionJson
        )
    }
    fun deleteDistribution(distribution: GameDistribution) {
        viewModelScope.launch {
            gameDistributionDao.deleteDistribution(distribution)
            loadDistributions()
        }
    }

    class Factory(
        private val application: Application
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HistoryViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}