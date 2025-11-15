// app/src/main/java/com/example/itrainer/ui/categories/CategoriesViewModel.kt
package com.example.itrainer.ui.categories

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.itrainer.data.database.ITrainerDatabase
import com.example.itrainer.data.entities.Category
import kotlinx.coroutines.launch

class CategoriesViewModel(application: Application) : AndroidViewModel(application) {
    private val database = ITrainerDatabase.getDatabase(application)
    private val categoryDao = database.categoryDao()

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    private val _navigationToTeams = MutableLiveData<Int?>()
    val navigationToTeams: LiveData<Int?> = _navigationToTeams

    init {
        loadCategories()
        initializeDefaultCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _categories.value = categoryDao.getAllCategories()
        }
    }

    private fun initializeDefaultCategories() {
        viewModelScope.launch {
            if (categoryDao.getAllCategories().isEmpty()) {

                // Categoría 1: Minibasket
                val minibasketCategory = Category(
                    name = "Minibasket",
                    periodsCount = 6,
                    playersPerPeriod = 5,
                    minPeriodsPerPlayer = 2,
                    maxPeriodsPerPlayer = 4,
                    minPlayers = 8,
                    maxPlayers = 15
                )
                categoryDao.insertCategory(minibasketCategory)

                // Categoría 2: Benjamín (EJEMPLO - Ajusta los valores según necesites)
//                val benjaminCategory = Category(
//                    name = "Benjamín",
//                    periodsCount = 4,           // 4 períodos de juego
//                    playersPerPeriod = 5,       // 5 jugadores en cancha
//                    minPeriodsPerPlayer = 2,    // Mínimo 2 períodos por jugador
//                    maxPeriodsPerPlayer = 3,    // Máximo 3 períodos por jugador
//                    minPlayers = 8,             // Mínimo 8 jugadores en el equipo
//                    maxPlayers = 12             // Máximo 12 jugadores en el equipo
//                )
//                categoryDao.insertCategory(benjaminCategory)

                // Categoría 3: Infantil y PreInf 2ª
                val infantilCategory = Category(
                    name = "Infantil y PreInf 2ª",
                    periodsCount = 4,           // 4 períodos
                    playersPerPeriod = 5,       // 5 jugadores en cancha
                    minPeriodsPerPlayer = 1,    // Mínimo 1 período por jugador (en los 3 primeros)
                    maxPeriodsPerPlayer = 4,    // Máximo 4 períodos por jugador (en total)
                    minPlayers = 8,             // Mínimo 8 jugadores
                    maxPlayers = 12             // Máximo 12 jugadores
                )
                categoryDao.insertCategory(infantilCategory)

                // Categoría 4: Cadete (EJEMPLO - Ajusta los valores según necesites)
//                val cadeteCategory = Category(
//                    name = "Cadete",
//                    periodsCount = 4,           // 4 cuartos
//                    playersPerPeriod = 5,       // 5 jugadores en cancha
//                    minPeriodsPerPlayer = 1,    // Mínimo 1 cuarto por jugador
//                    maxPeriodsPerPlayer = 4,    // Máximo 4 cuartos por jugador
//                    minPlayers = 8,             // Mínimo 8 jugadores
//                    maxPlayers = 15             // Máximo 15 jugadores
//                )
//                categoryDao.insertCategory(cadeteCategory)

                // Añade más categorías aquí si lo necesitas...

                loadCategories()
            }
        }
    }

    fun onCategorySelected(categoryId: Int) {
        _navigationToTeams.value = categoryId
    }

    fun onNavigationToTeamsComplete() {
        _navigationToTeams.value = null
    }
}