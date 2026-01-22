package com.cipecma.flexit.network

import okhttp3.OkHttpClient
import retrofit2.http.*
import retrofit2.http.FormUrlEncoded

data class LoginRequest(val email: String, val password: String)
data class LoginResponse(val token: String)
interface ApiService {
    @FormUrlEncoded
    @POST("auth/login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): LoginResponse

    //Les autres appels AOI ICIs
}