package com.example.quanlythuvien.ui.login

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
        data class Success(
            val accessToken: String,
            val refreshToken: String,
            val role: String,
            val username: String
        ) : LoginState()
        data class Error(val error: String) : LoginState()
}