package com.cipecma.flexit

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
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

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

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
        binding.appBarMain.fab?.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .setAnchorView(R.id.fab).show()
        }

        // NavController
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val navController = navHostFragment.navController

        // Drawer Navigation pour les items avec fragments
        binding.navView?.let { navView ->
            appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.nav_transform, R.id.nav_reflow, R.id.nav_slideshow, R.id.nav_settings
                ),
                binding.drawerLayout
            )
            setupActionBarWithNavController(navController, appBarConfiguration)

            // Configure seulement les items liés aux fragments
            navView.setupWithNavController(navController)

            // Listener manuel pour logout
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
                    R.id.nav_transform, R.id.nav_reflow, R.id.nav_slideshow
                )
            )
            setupActionBarWithNavController(navController, bottomAppBarConfig)
            bottomNav.setupWithNavController(navController)
        }
    }

    // Fonction de logout
    private fun logout() {
        AuthManager.clearToken()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val result = super.onCreateOptionsMenu(menu)
        val navView: NavigationView? = findViewById(R.id.nav_view)
        if (navView == null) {
            menuInflater.inflate(R.menu.overflow, menu)
        }
        return result
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_settings -> {
                val navController = findNavController(R.id.nav_host_fragment_content_main)
                navController.navigate(R.id.nav_settings)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
