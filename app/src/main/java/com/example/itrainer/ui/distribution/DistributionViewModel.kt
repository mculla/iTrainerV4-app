package com.example.itrainer.ui.distribution

import android.app.Application
import androidx.lifecycle.*
import com.example.itrainer.data.database.ITrainerDatabase
import com.example.itrainer.data.entities.Category
import com.example.itrainer.data.entities.GameDistribution
import com.example.itrainer.data.entities.Player
import com.example.itrainer.data.entities.Team
import com.example.itrainer.data.models.DistributionModel
import com.example.itrainer.data.models.PlayerModel
import com.example.itrainer.data.models.SubstitutionModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import java.util.Date

class DistributionViewModel(
    application: Application,
    private val teamId: Int,
    private val categoryId: Int
) : AndroidViewModel(application) {

    private val database = ITrainerDatabase.getDatabase(application)
    private val playerDao = database.playerDao()
    private val teamDao = database.teamDao()
    private val categoryDao = database.categoryDao()

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        encodeDefaults = true
    }

    // Lista completa de jugadores del equipo
    private val _allTeamPlayers = MutableLiveData<List<Player>>()
    val allTeamPlayers: LiveData<List<Player>> = _allTeamPlayers

    // Lista de jugadores disponibles para el partido
    private val _availablePlayers = MutableLiveData<List<Player>>()
    val availablePlayers: LiveData<List<Player>> = _availablePlayers

    // Distribución actual de jugadores por período
    private val _distribution = MutableStateFlow<Map<Int, List<Player>>>(emptyMap())
    val distribution = _distribution.asStateFlow()

    // Mensajes de validación
    private val _validationMessages = MutableLiveData<List<String>>()
    val validationMessages: LiveData<List<String>> = _validationMessages

    // Control de diálogo de guardado
    private val _showSaveDialog = MutableLiveData<Boolean>()
    val showSaveDialog: LiveData<Boolean> = _showSaveDialog

    // Estado de guardado completado
    private val _saveComplete = MutableLiveData<Boolean>()
    val saveComplete: LiveData<Boolean> = _saveComplete

    private val _maxPlayersReachedEvent = MutableLiveData<Int>()
    val maxPlayersReachedEvent: LiveData<Int> = _maxPlayersReachedEvent

    private var category: Category? = null

    private val _substitutions = MutableStateFlow<Map<Int, SubstitutionInfo>>(emptyMap())
    val substitutions = _substitutions.asStateFlow()

    init {
        viewModelScope.launch {
            category = categoryDao.getCategoryById(categoryId)
            initializeDistribution()
            loadPlayers()
        }
    }

    private fun loadPlayers() {
        viewModelScope.launch {
            _allTeamPlayers.value = playerDao.getPlayersByTeam(teamId)
            _availablePlayers.value = _allTeamPlayers.value
        }
    }

    private fun initializeDistribution() {
        val periodCount = category?.periodsCount ?: return
        val initialDistribution = mutableMapOf<Int, List<Player>>()
        for (i in 1..periodCount) {
            initialDistribution[i] = emptyList()
        }
        _distribution.value = initialDistribution
    }

    fun updatePlayerAvailability(playerId: Int, isAvailable: Boolean) {
        val currentPlayers = _availablePlayers.value?.toMutableList() ?: mutableListOf()
        val allPlayers = _allTeamPlayers.value ?: return

        if (isAvailable) {
            allPlayers.find { it.id == playerId }?.let {
                if (!currentPlayers.contains(it)) {
                    currentPlayers.add(it)
                }
            }
        } else {
            currentPlayers.removeAll { it.id == playerId }
            // Eliminar al jugador de todos los períodos si estaba asignado
            removePlayerFromAllPeriods(playerId)
        }

        _availablePlayers.value = currentPlayers
        validateDistribution()
    }

    private fun removePlayerFromAllPeriods(playerId: Int) {
        val currentDistribution = _distribution.value.toMutableMap()
        currentDistribution.forEach { (period, players) ->
            currentDistribution[period] = players.filterNot { it.id == playerId }
        }
        _distribution.value = currentDistribution
    }

    fun togglePlayerInPeriod(playerId: Int, period: Int, selected: Boolean) {
        viewModelScope.launch {
            val currentDistribution = _distribution.value.toMutableMap()
            val periodPlayers = currentDistribution[period]?.toMutableList() ?: mutableListOf()
            val player = playerDao.getPlayerById(playerId) ?: return@launch

            val maxPlayersPerPeriod = category?.playersPerPeriod ?: 5
            if (selected && periodPlayers.size >= maxPlayersPerPeriod) {
                _maxPlayersReachedEvent.value = period
                return@launch
            }

            if (selected) {
                if (!periodPlayers.contains(player)) {
                    periodPlayers.add(player)
                }
            } else {
                periodPlayers.remove(player)
            }

            currentDistribution[period] = periodPlayers
            _distribution.value = currentDistribution
            validateDistribution()
        }
    }

    fun isPeriodFull(period: Int): Boolean {
        val maxPlayersPerPeriod = category?.playersPerPeriod ?: 5
        return _distribution.value[period]?.size ?: 0 >= maxPlayersPerPeriod
    }

    fun toggleSubstitution(playerId: Int, period: Int, isOut: Boolean) {
        val key = period * 1000 + playerId
        val currentSubs = _substitutions.value.toMutableMap()

        if (isOut) {
            currentSubs[key] = SubstitutionInfo(playerId, isOut = true, isSubstitute = false)
        } else {
            currentSubs.remove(key)
        }

        _substitutions.value = currentSubs
    }

    fun addSubstitute(playerId: Int, period: Int) {
        val key = period * 1000 + playerId
        val currentSubs = _substitutions.value.toMutableMap()
        currentSubs[key] = SubstitutionInfo(playerId, isOut = false, isSubstitute = true)
        _substitutions.value = currentSubs
    }

    fun removeSubstitute(playerId: Int, period: Int) {
        val key = period * 1000 + playerId
        val currentSubs = _substitutions.value.toMutableMap()
        currentSubs.remove(key)
        _substitutions.value = currentSubs
    }

    fun getSubstitutesCount(period: Int): Int {
        return _substitutions.value.values.count { it.isSubstitute }
    }

    fun isLastPeriod(period: Int): Boolean {
        return period == category?.periodsCount
    }

    private fun validateDistribution() {
        val messages = mutableListOf<String>()
        val cat = category ?: return
        val dist = _distribution.value
        val allPlayers = dist.values.flatten().distinct()

        // Validar número de jugadores por período
        dist.forEach { (period, players) ->
            if (players.size != cat.playersPerPeriod) {
                messages.add("Período $period: Se necesitan ${cat.playersPerPeriod} jugadores")
            }
        }

        // Contar períodos por jugador
        val playerPeriodCounts = countPlayerPeriods()

        // Validar períodos mínimos y máximos por jugador
        allPlayers.forEach { player ->
            val periodsPlayed = playerPeriodCounts[player] ?: 0
            if (periodsPlayed < cat.minPeriodsPerPlayer) {
                messages.add("${player.name} debe jugar al menos ${cat.minPeriodsPerPlayer} períodos")
            }
            if (periodsPlayed > cat.maxPeriodsPerPlayer) {
                messages.add("${player.name} no puede jugar más de ${cat.maxPeriodsPerPlayer} períodos")
            }
        }

        applySpecialRules(cat, allPlayers, playerPeriodCounts, messages)

        _validationMessages.value = messages
    }

    private fun applySpecialRules(
        cat: Category,
        allPlayers: List<Player>,
        playerPeriodCounts: Map<Player, Int>,
        messages: MutableList<String>
    ) {
        when (cat.name) {
            "Minibasket" -> {
                if (allPlayers.size == 8 && cat.periodsCount >= 5) {
                    val playersWithFourPeriods = playerPeriodCounts.filter { it.value >= 4 }
                    if (playersWithFourPeriods.isEmpty()) {
                        messages.add("Con 8 jugadores, uno debe jugar 4 de los 5 primeros períodos")
                    }
                }
            }

            "Infantil y PreInf 2ª" -> {
                if (cat.periodsCount >= 3) {
                    val firstThreePeriods = _distribution.value.filterKeys { period -> period <= 3 }
                    val playerCountInFirstThree = firstThreePeriods.values
                        .flatten()
                        .groupingBy { player -> player }
                        .eachCount()

                    allPlayers.forEach { player ->
                        val periodsInFirstThree = playerCountInFirstThree[player] ?: 0
                        if (periodsInFirstThree == 0) {
                            messages.add("${player.name} debe jugar al menos 1 de los 3 primeros períodos")
                        }
                        if (periodsInFirstThree > 2) {
                            messages.add("${player.name} no puede jugar más de 2 de los 3 primeros períodos")
                        }
                    }
                }
            }
        }
    }

    private fun countPlayerPeriods(): Map<Player, Int> {
        return _distribution.value.values
            .flatten()
            .groupingBy { it }
            .eachCount()
    }

    fun showSaveDistributionDialog() {
        if (_validationMessages.value?.isEmpty() == true) {
            _showSaveDialog.value = true
        }
    }

    fun saveDistribution(gameDate: String, opponent: String) {
        if (_validationMessages.value?.isNotEmpty() == true) return

        viewModelScope.launch {
            // NUEVO: Convertir sustituciones a formato serializable
            val substitutionsMap = _substitutions.value.map { (key, info) ->
                key.toString() to SubstitutionModel(
                    playerId = info.playerId,
                    period = key / 1000,
                    isOut = info.isOut,
                    isSubstitute = info.isSubstitute
                )
            }.toMap()

            val distributionModel = DistributionModel(
                periods = _distribution.value.mapValues { (_, players) ->
                    players.map { PlayerModel(it.id, it.name, it.number) }
                },
                date = System.currentTimeMillis(),
                gameDate = gameDate,
                opponent = opponent,
                substitutions = substitutionsMap // NUEVO: Guardar las sustituciones
            )

            val distribution = GameDistribution(
                teamId = teamId,
                date = Date(),
                gameDate = gameDate,
                opponent = opponent,
                distributionJson = json.encodeToString(distributionModel)
            )

            database.gameDistributionDao().insertDistribution(distribution)
            _saveComplete.value = true
        }
    }

    class Factory(
        private val application: Application,
        private val teamId: Int,
        private val categoryId: Int
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DistributionViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DistributionViewModel(application, teamId, categoryId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

data class SubstitutionInfo(
    val playerId: Int,
    val isOut: Boolean = false,
    val isSubstitute: Boolean = false
)
