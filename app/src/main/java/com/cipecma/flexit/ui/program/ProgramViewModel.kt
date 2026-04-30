package com.cipecma.flexit.ui.program

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cipecma.flexit.network.ApiService
import com.cipecma.flexit.network.RetrofitClient
import kotlinx.coroutines.launch

class ProgramViewModel : ViewModel() {

    // On observe une liste d'objets Program
    private val _programNames = MutableLiveData<List<ApiService.Program>>()
    val programNames: LiveData<List<ApiService.Program>> = _programNames

    fun fetchPrograms(userId: Int?) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.getProgramNames(userId)

                // LOG : On affiche le nombre d'éléments reçus
                Log.d("DEBUG_API", "Succès ! Nombre de programmes : ${response.size}")

                // LOG : On affiche le premier élément pour vérifier le contenu
                if (response.isNotEmpty()) {
                    Log.d("DEBUG_API", "Premier programme : ${response[0].name}")
                }

                _programNames.value = response
            } catch (e: Exception) {
                // LOG : L'erreur CRITIQUE (URL, format, réseau...)
                Log.e("DEBUG_API", "ERREUR LORS DE L'APPEL", e)
                _programNames.value = emptyList()
            }
        }
    }

    fun createProgram(name: String, userId: Int?) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.createProgram(name, userId)
                if (response.isSuccessful && response.body()?.success == true) {
                    fetchPrograms(userId) // On rafraîchit la liste
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Erreur création : ${e.message}")
            }
        }
    }

    fun updateProgram(id: Int?, newName: String, userId: Int?){
        viewModelScope.launch {
            try {
                Log.d("API_CALL", "Envoi vers Repository -> ID: $id, Name: $newName, User: $userId")
                val response = RetrofitClient.api.updateProgram(id, newName, userId)
                if (response.isSuccessful && response.body()?.success == true) {
                    fetchPrograms(userId) // On rafraîchit la liste
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Erreur création : ${e.message}")
            }
        }
    }

    fun deleteProgram(programId: Int, userId: Int?) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.deleteProgram(programId, userId)

                if (response.isSuccessful && response.body()?.success == true) {
                    fetchPrograms(userId)
                } else {
                    Log.e("API_DEBUG", "Réponse : ${response.body()?.message}")
                }
            } catch (e: Exception) {
                Log.e("API_DEBUG", "Erreur : ${e.message}")
            }
        }
    }
}