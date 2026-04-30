package com.cipecma.flexit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.cipecma.flexit.auth.AuthManager
import com.cipecma.flexit.network.RetrofitClient
import kotlinx.coroutines.launch
import retrofit2.HttpException
import retrofit2.Retrofit

class LoginActivity : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkIfAlreadyLoggedIn()

        setContentView(R.layout.activity_login)

        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        loginButton = findViewById(R.id.loginButton)

        loginButton.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                login(email, password)
            } else {
                Toast.makeText(this, email, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkIfAlreadyLoggedIn() {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val savedToken = prefs.getString("token", null)
        val savedUserId = prefs.getInt("user_id", -1)
        if (savedToken != null && savedUserId != -1) {
            AuthManager.setToken(savedToken)
            AuthManager.setUserId(savedUserId)
            goToMainActivity()
        }
    }

    private fun login(email: String, password: String){
        Log.i("LOGIN", email)
        Log.i("PASSWORD", password)
        lifecycleScope.launch {
            try {
                //Appel de notre fonction pour l'API
                val response = RetrofitClient.api.login(email, password)

                //Stocker le jeton en mémoire
                AuthManager.setToken(response.token)
                //Stocker l'id du user en mémoire
                AuthManager.setUserId(response.id_user)

                //Sauvegarder le jeton de manière persistante (DISQUE DUR)
                getSharedPreferences("auth", MODE_PRIVATE)
                    .edit {
                        putString("token", response.token)
                        putInt("user_id", response.id_user)
                    }

                Toast.makeText(
                    this@LoginActivity,
                    "Connexion réussie !",
                    Toast.LENGTH_SHORT
                ).show()

                goToMainActivity()

            } catch (e: HttpException) {
                //Gestion des erreur HTTP
                when (e.code()) {
                    401 -> {
                        Toast.makeText(
                            this@LoginActivity,
                            "Email ou mot de passe incorrect",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    else -> {
                        Toast.makeText(
                            this@LoginActivity,
                            "Erreur serveur ${e.code()}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@LoginActivity,
                    "Erreur de connexion: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() //Empêcher de revenir au login avec le bouton retour
    }
}