// app/src/main/java/com/example/itrainer/ui/teams/TeamsViewModel.kt
package com.example.itrainer.ui.teams

import android.app.Application
import androidx.lifecycle.*
import com.example.itrainer.data.database.ITrainerDatabase
import com.example.itrainer.data.entities.Team
import kotlinx.coroutines.launch

class TeamsViewModel(
    application: Application,
    private val categoryId: Int
) : AndroidViewModel(application) {

    private val database = ITrainerDatabase.getDatabase(application)
    private val teamDao = database.teamDao()
    private val categoryDao = database.categoryDao()

    private val _teams = MutableLiveData<List<Team>>()
    val teams: LiveData<List<Team>> = _teams

    private val _categoryName = MutableLiveData<String>()
    val categoryName: LiveData<String> = _categoryName

    private val _navigateToDistribution = MutableLiveData<Pair<Int, Int>?>()
    val navigateToDistribution: LiveData<Pair<Int, Int>?> = _navigateToDistribution

    init {
        loadTeams()
        loadCategoryName()
    }

    private fun loadTeams() {
        viewModelScope.launch {
            _teams.value = teamDao.getTeamsByCategory(categoryId)
        }
    }

    private fun loadCategoryName() {
        viewModelScope.launch {
            val category = categoryDao.getCategoryById(categoryId)
            _categoryName.value = category?.name ?: ""
        }
    }

    fun createTeam(name: String) {
        viewModelScope.launch {
            val team = Team(name = name, categoryId = categoryId)
            teamDao.insertTeam(team)
            loadTeams()
        }
    }

    fun deleteTeam(team: Team) {
        viewModelScope.launch {
            teamDao.deleteTeam(team)
            loadTeams()
        }
    }

    fun onTeamSelected(teamId: Int) {
        _navigateToDistribution.value = Pair(teamId, categoryId)
    }

    fun onNavigationComplete() {
        _navigateToDistribution.value = null
    }

    class Factory(
        private val application: Application,
        private val categoryId: Int
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TeamsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TeamsViewModel(application, categoryId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

