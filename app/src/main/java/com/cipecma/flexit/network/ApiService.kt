package com.cipecma.flexit.network

import retrofit2.Response
import retrofit2.http.*
import retrofit2.http.FormUrlEncoded

interface ApiService {

    @FormUrlEncoded
    @POST("auth/login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): LoginResponse

    // Récupérer les programmes d'un utilisateur
    @GET("programs/user/{id}")
    suspend fun getProgramNames(@Path("id") userId: Int?): List<Program>

    // Détail complet d'un programme (programme + workouts + exercises + series)
    @GET("programs/{id}")
    suspend fun getProgramDetail(@Path("id") programId: Int): ProgramDetail

    // Catalogue complet des exercices
    @GET("exercices/all")
    suspend fun getAllExercises(): List<ExerciseCatalog>

    // Réécrire toutes les séances d'un programme
    @POST("programs/workout/save/{id}")
    suspend fun saveWorkouts(
        @Path("id") programId: Int,
        @Body body: SaveWorkoutsBody,
    ): Response<SaveResponse>

    // --- NOUVELLE MÉTHODE POUR LA SUPPRESSION ---
    @FormUrlEncoded
    @POST("programs/delete")
    suspend fun deleteProgram(
        @Field("id") id: Int,
        @Field("id_user") userId: Int?
    ): Response<DeleteResponse>

    // --- NOUVELLE MÉTHODE POUR LA CRÉATION ---
    @FormUrlEncoded
    @POST("programs/create")
    suspend fun createProgram(
        @Field("name") name: String,
        @Field("id_user") userId: Int?
    ): Response<CreateResponse>

    // --- NOUVELLE MÉTHODE POUR LA MISE A JOUR ---
    @FormUrlEncoded
    @POST("programs/update/{id}")
    suspend fun updateProgram(
        @Path("id") id: Int?,
        @Field("name") name: String,
        @Field("id_user") userId: Int?,
    ): Response<UpdateResponse>

    // --- DATA CLASSES ---

    data class Program(
        val id: Int, // Changé en Int pour correspondre à l'ID de la BDD
        val name: String
    )

    data class DeleteResponse(
        val success: Boolean,
        val message: String
    )

    data class CreateResponse(
        val success: Boolean,
        val message: String,
    )

    data class UpdateResponse(
        val success: Boolean,
        val message: String,
    )

    data class LoginRequest(val email: String, val password: String)
    data class LoginResponse(
        val token: String,
        val id_user : Int,
    )
}