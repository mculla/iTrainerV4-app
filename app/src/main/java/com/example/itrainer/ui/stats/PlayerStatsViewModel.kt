// app/src/main/java/com/example/itrainer/ui/stats/PlayerStatsViewModel.kt
package com.example.itrainer.ui.stats

import android.app.Application
import androidx.lifecycle.*
import com.example.itrainer.data.database.ITrainerDatabase
import com.example.itrainer.data.entities.Player
import com.example.itrainer.data.entities.PlayerStats
import kotlinx.coroutines.launch
import java.util.Calendar

data class PlayerWithStats(
    val player: Player,
    val stats: PlayerStats
)

class PlayerStatsViewModel(
    application: Application,
    private val teamId: Int
) : AndroidViewModel(application) {

    private val database = ITrainerDatabase.getDatabase(application)
    private val playerDao = database.playerDao()
    private val playerStatsDao = database.playerStatsDao()

    private val _playerStats = MutableLiveData<List<PlayerWithStats>>()
    val playerStats: LiveData<List<PlayerWithStats>> = _playerStats

    private val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    init {
        loadPlayerStats()
    }

    private fun loadPlayerStats() {
        viewModelScope.launch {
            val players = playerDao.getPlayersByTeam(teamId)
            val statsWithPlayers = players.mapNotNull { player ->
                val stats = playerStatsDao.getPlayerStats(player.id, currentYear)
                if (stats != null) {
                    PlayerWithStats(player, stats)
                } else {
                    // Crear estadísticas vacías si no existen
                    val emptyStats = PlayerStats(
                        playerId = player.id,
                        seasonYear = currentYear
                    )
                    PlayerWithStats(player, emptyStats)
                }
            }.sortedByDescending { it.stats.totalPeriods }

            _playerStats.value = statsWithPlayers
        }
    }

    class Factory(
        private val application: Application,
        private val teamId: Int
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PlayerStatsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PlayerStatsViewModel(application, teamId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}