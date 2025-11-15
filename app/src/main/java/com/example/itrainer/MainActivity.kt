// app/src/main/java/com/example/itrainer/MainActivity.kt
package com.example.itrainer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.itrainer.databinding.ActivityMainBinding
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.navigateUp

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar la Toolbar
        setSupportActionBar(binding.toolbar)

        setupNavigation()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Configurar los destinos de nivel superior
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.categoriesFragment,
                R.id.teamsFragment,
                R.id.distributionFragment,
                R.id.historyFragment,
                R.id.rulesFragment
            )
        )

        // Configurar la ActionBar con la navegación
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Configurar el BottomNavigationView
        binding.bottomNavigation.setupWithNavController(navController)

        // Manejar la navegación del menú inferior
        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.categoriesFragment -> {
                    navController.navigate(R.id.categoriesFragment)
                    true
                }
                R.id.teamsFragment -> {
                    // Si no hay categoría seleccionada, ir a categorías primero
                    if (navController.currentDestination?.id != R.id.teamsFragment) {
                        navController.navigate(R.id.categoriesFragment)
                    }
                    true
                }
                R.id.distributionFragment -> {
                    // Si no hay equipo seleccionado, ir a categorías primero
                    if (navController.currentDestination?.id != R.id.distributionFragment) {
                        navController.navigate(R.id.categoriesFragment)
                    }
                    true
                }
                R.id.historyFragment -> {
                    navController.navigate(R.id.historyFragment)
                    true
                }
                R.id.rulesFragment -> {
                    navController.navigate(R.id.rulesFragment)
                    true
                }
                else -> false
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}