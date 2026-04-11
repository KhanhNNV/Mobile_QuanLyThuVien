package com.example.quanlythuvien.ui.register

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    data class Success(
        val accessToken: String,
        val refreshToken: String,
        val message: String
    ) : RegisterState()
    data class Error(val error: String) : RegisterState()
}