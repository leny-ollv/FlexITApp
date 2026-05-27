package com.cipecma.flexit.network

import com.google.gson.annotations.SerializedName

data class ProgramDetail(
    val id: String?,
    val name: String?,
    @SerializedName("id_user") val idUser: String?,
    val workouts: List<Workout>?,
)

data class Workout(
    val id: String?,
    val date: String?,
    val exercises: List<WorkoutExercise>?,
)

data class WorkoutExercise(
    @SerializedName("id_program") val idProgram: String?,
    @SerializedName("id_exercice") val idExercice: String?,
    val date: String?,
    @SerializedName("rest_time") val restTime: String?,
    val order: String?,
    val series: List<Serie>?,
)

data class Serie(
    @SerializedName("id_program") val idProgram: String?,
    @SerializedName("id_exercice") val idExercice: String?,
    val reps: String?,
    val weight: String?,
    val date: String?,
)

data class ExerciseCatalog(
    val id: String?,
    val name: String?,
)

data class SaveResponse(
    val success: Boolean,
    val message: String?,
)

data class SaveWorkoutsBody(
    val workouts: List<SaveWorkoutDto>,
)

data class SaveWorkoutDto(
    val date: String,
    val exercises: List<SaveExerciseDto>,
)

data class SaveExerciseDto(
    @SerializedName("id_exercice") val idExercice: Int,
    @SerializedName("rest_time") val restTime: String,
    val order: Int,
    val series: List<SaveSerieDto>,
)

data class SaveSerieDto(
    val reps: Int,
    val weight: Int,
)
