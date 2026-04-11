package com.example.quanlythuvien.data.repository

import com.example.quanlythuvien.data.model.request.LoginRequest
import com.example.quanlythuvien.data.model.request.RegisterRequest
import com.example.quanlythuvien.data.remote.AuthApiService

class AuthRepository(private val apiService: AuthApiService) {
    suspend fun register(request: RegisterRequest) = apiService.register(request)

    suspend fun login(request: LoginRequest) = apiService.login(request)
}