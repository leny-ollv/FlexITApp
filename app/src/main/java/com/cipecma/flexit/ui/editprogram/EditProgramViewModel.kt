package com.cipecma.flexit.ui.editprogram

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cipecma.flexit.network.ExerciseCatalog
import com.cipecma.flexit.network.RetrofitClient
import com.cipecma.flexit.network.SaveExerciseDto
import com.cipecma.flexit.network.SaveSerieDto
import com.cipecma.flexit.network.SaveWorkoutDto
import com.cipecma.flexit.network.SaveWorkoutsBody
import kotlinx.coroutines.launch

class EditProgramViewModel : ViewModel() {

    data class SerieDraft(var reps: Int = 0, var weight: Int = 0)
    data class ExerciseDraft(
        var idExercice: Int = 0,
        var restTime: String = "00:01:00",
        var order: Int = 1,
        val series: MutableList<SerieDraft> = mutableListOf(),
    )
    data class WorkoutDraft(
        var date: String = "",
        val exercises: MutableList<ExerciseDraft> = mutableListOf(),
    )

    private val _workouts = MutableLiveData<MutableList<WorkoutDraft>>(mutableListOf())
    val workouts: LiveData<MutableList<WorkoutDraft>> = _workouts

    private val _catalog = MutableLiveData<List<ExerciseCatalog>>(emptyList())
    val catalog: LiveData<List<ExerciseCatalog>> = _catalog

    private val _saveResult = MutableLiveData<Result<String>?>()
    val saveResult: LiveData<Result<String>?> = _saveResult

    var currentProgramId: Int = -1
        private set

    var programName: String = ""

    fun load(programId: Int) {
        currentProgramId = programId
        viewModelScope.launch {
            try {
                val program = RetrofitClient.api.getProgramDetail(programId)
                programName = program.name.orEmpty()
                val drafts = program.workouts.orEmpty().map { w ->
                    WorkoutDraft(
                        date = w.date.orEmpty(),
                        exercises = w.exercises.orEmpty().map { e ->
                            ExerciseDraft(
                                idExercice = e.idExercice?.toIntOrNull() ?: 0,
                                restTime = e.restTime ?: "00:01:00",
                                order = e.order?.toIntOrNull() ?: 1,
                                series = e.series.orEmpty().map { s ->
                                    SerieDraft(
                                        reps = s.reps?.toIntOrNull() ?: 0,
                                        weight = s.weight?.toIntOrNull() ?: 0,
                                    )
                                }.toMutableList(),
                            )
                        }.toMutableList(),
                    )
                }.toMutableList()
                _workouts.value = drafts
            } catch (e: Exception) {
                Log.e("EditProgramVM", "load error", e)
                _workouts.value = mutableListOf()
            }
        }

        viewModelScope.launch {
            try {
                _catalog.value = RetrofitClient.api.getAllExercises()
            } catch (e: Exception) {
                Log.e("EditProgramVM", "catalog error", e)
            }
        }
    }

    fun touchWorkouts() {
        _workouts.value = _workouts.value
    }

    fun addWorkout(date: String) {
        val list = _workouts.value ?: mutableListOf()
        list.add(WorkoutDraft(date = date))
        _workouts.value = list
    }

    fun removeWorkout(index: Int) {
        val list = _workouts.value ?: return
        if (index in list.indices) {
            list.removeAt(index)
            _workouts.value = list
        }
    }

    fun addExercise(workoutIndex: Int) {
        val list = _workouts.value ?: return
        val w = list.getOrNull(workoutIndex) ?: return
        w.exercises.add(ExerciseDraft(order = w.exercises.size + 1))
        _workouts.value = list
    }

    fun removeExercise(workoutIndex: Int, exerciseIndex: Int) {
        val list = _workouts.value ?: return
        val w = list.getOrNull(workoutIndex) ?: return
        if (exerciseIndex in w.exercises.indices) {
            w.exercises.removeAt(exerciseIndex)
            _workouts.value = list
        }
    }

    fun addSerie(workoutIndex: Int, exerciseIndex: Int) {
        val list = _workouts.value ?: return
        val e = list.getOrNull(workoutIndex)?.exercises?.getOrNull(exerciseIndex) ?: return
        e.series.add(SerieDraft())
        _workouts.value = list
    }

    fun removeSerie(workoutIndex: Int, exerciseIndex: Int, serieIndex: Int) {
        val list = _workouts.value ?: return
        val e = list.getOrNull(workoutIndex)?.exercises?.getOrNull(exerciseIndex) ?: return
        if (serieIndex in e.series.indices) {
            e.series.removeAt(serieIndex)
            _workouts.value = list
        }
    }

    fun save(programId: Int) {
        val list = _workouts.value ?: return
        val body = SaveWorkoutsBody(
            workouts = list.map { w ->
                SaveWorkoutDto(
                    date = w.date,
                    exercises = w.exercises.mapIndexed { idx, e ->
                        SaveExerciseDto(
                            idExercice = e.idExercice,
                            restTime = e.restTime,
                            order = idx + 1,
                            series = e.series.map { s ->
                                SaveSerieDto(reps = s.reps, weight = s.weight)
                            },
                        )
                    },
                )
            },
        )

        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.saveWorkouts(programId, body)
                val ok = response.isSuccessful && response.body()?.success == true
                _saveResult.value = if (ok) {
                    Result.success(response.body()?.message ?: "Sauvegardé")
                } else {
                    Result.failure(Exception(response.body()?.message ?: "Erreur ${response.code()}"))
                }
            } catch (e: Exception) {
                Log.e("EditProgramVM", "save error", e)
                _saveResult.value = Result.failure(e)
            }
        }
    }

    fun consumeSaveResult() {
        _saveResult.value = null
    }
}
