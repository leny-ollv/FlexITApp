package com.cipecma.flexit

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.NavigationUI
import com.cipecma.flexit.auth.AuthManager
import com.cipecma.flexit.databinding.ActivityMainBinding
import com.cipecma.flexit.ui.program.ProgramViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val programViewModel: ProgramViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Si pas connecté, redirige vers LoginActivity
        if (!AuthManager.isLoggedIn()) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarMain.toolbar)

        // Exemple FAB
        binding.appBarMain.fab?.setOnClickListener {
            val inflater = layoutInflater
            val dialogLayout = inflater.inflate(R.layout.create_program, null)
            val editText = dialogLayout.findViewById<EditText>(R.id.editProgramName)

            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Créer un programme")
                .setView(dialogLayout)
                .setPositiveButton("Enregistrer") { _, _ ->
                    val nom = editText.text.toString()
                    if (nom.isNotBlank()) {
                        // Récupération de l'id du user
                        val currentUserId = AuthManager.getUserId()
                        // Si l'id est bien configuré (> 0), on lance l'appel
                        if (currentUserId != -1) {
                            programViewModel.fetchPrograms(userId = currentUserId)
                        }
                        programViewModel.createProgram(nom, currentUserId)
                        Toast.makeText(this, "Création du programme...", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Annuler", null)
                .show()
        }

        // NavController
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val navController = navHostFragment.navController

        // Drawer Navigation pour les items avec fragments
        binding.navView?.let { navView ->
            appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.nav_program, R.id.nav_settings
                ),
                binding.drawerLayout
            )
            setupActionBarWithNavController(navController, appBarConfiguration)

            // Configure seulement les items liés aux fragments
            navView.setupWithNavController(navController)

            // Listener manuel pour logout (Menu Latéral)
            navView.setNavigationItemSelectedListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.nav_logout -> {
                        logout()
                        true
                    }
                    else -> {
                        val handled = NavigationUI.onNavDestinationSelected(menuItem, navController)
                        if (handled) binding.drawerLayout?.closeDrawers()
                        handled
                    }
                }
            }
        }

        // BottomNavigation si existant
        binding.appBarMain.contentMain.bottomNavView?.let { bottomNav ->
            val bottomAppBarConfig = AppBarConfiguration(
                setOf(
                    R.id.nav_program, R.id.nav_settings
                )
            )
            setupActionBarWithNavController(navController, bottomAppBarConfig)
            bottomNav.setupWithNavController(navController)
        }
    }

    // Fonction de logout
    private fun logout() {
        AuthManager.clearToken()

        val sharedPref = getSharedPreferences("auth", MODE_PRIVATE)
        sharedPref.edit().clear().apply()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // MODIFICATION : On affiche le menu overflow (celui en haut à droite)
        menuInflater.inflate(R.menu.overflow, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // MODIFICATION : Gestion du clic sur Logout dans le menu du haut
        return when (item.itemId) {
            R.id.nav_logout -> {
                logout() // Appel de la fonction de redirection
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}