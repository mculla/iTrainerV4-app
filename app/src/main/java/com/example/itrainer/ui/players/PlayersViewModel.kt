// app/src/main/java/com/example/itrainer/ui/players/PlayersViewModel.kt
package com.example.itrainer.ui.players

import android.app.Application
import androidx.lifecycle.*
import com.example.itrainer.data.database.ITrainerDatabase
import com.example.itrainer.data.entities.Player
import com.example.itrainer.data.entities.Team
import kotlinx.coroutines.launch

class PlayersViewModel(
    application: Application,
    private val teamId: Int
) : AndroidViewModel(application) {

    private val database = ITrainerDatabase.getDatabase(application)
    private val playerDao = database.playerDao()
    private val teamDao = database.teamDao()

    private val _players = MutableLiveData<List<Player>>()
    val players: LiveData<List<Player>> = _players

    private val _team = MutableLiveData<Team>()
    val team: LiveData<Team> = _team

    init {
        loadPlayers()
        loadTeam()
    }

    private fun loadPlayers() {
        viewModelScope.launch {
            _players.value = playerDao.getPlayersByTeam(teamId)
        }
    }

    private fun loadTeam() {
        viewModelScope.launch {
            _team.value = teamDao.getTeamById(teamId)
        }
    }

    fun createPlayer(name: String, number: Int) {
        viewModelScope.launch {
            val player = Player(
                teamId = teamId,
                name = name,
                number = number
            )
            playerDao.insertPlayer(player)
            loadPlayers()
        }
    }

    fun updatePlayer(player: Player, newName: String, newNumber: Int) {
        viewModelScope.launch {
            val updatedPlayer = player.copy(name = newName, number = newNumber)
            playerDao.updatePlayer(updatedPlayer)
            loadPlayers()
        }
    }

    fun deletePlayer(player: Player) {
        viewModelScope.launch {
            playerDao.deletePlayer(player)
            loadPlayers()
        }
    }

    class Factory(
        private val application: Application,
        private val teamId: Int
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PlayersViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PlayersViewModel(application, teamId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

