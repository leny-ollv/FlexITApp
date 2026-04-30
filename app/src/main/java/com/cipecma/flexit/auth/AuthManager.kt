package com.cipecma.flexit.auth

object AuthManager {
    private var token: String? = null
    private var id_user: Int = -1

    fun setToken(newToken: String) {
        token = newToken
    }

    fun getToken(): String? = token

    fun setUserId(id: Int) {
        id_user = id
    }
    fun getUserId(): Int = id_user

    fun isLoggedIn(): Boolean = token != null

    fun clearToken() {
        token = null
        id_user = -1
    }
}