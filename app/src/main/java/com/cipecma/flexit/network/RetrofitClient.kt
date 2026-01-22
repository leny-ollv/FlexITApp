package com.cipecma.flexit.network

import com.cipecma.flexit.auth.AuthManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://slam.cipecma.net/2426/lolliveau/flexit/api/"

    //Client sans authentification (pour le login)
    private val publicClient = OkHttpClient.Builder().build()

    private val authentificatedClient: OkHttpClient
        get() = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val token = AuthManager.getToken()
                val request = if (token != null) {
                    chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer $token")
                        .build()
                } else {
                    chain.request()
                }
                chain.proceed(request)
            }
            .build()

    val api: ApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(authentificatedClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)
}