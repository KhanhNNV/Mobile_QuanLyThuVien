package com.example.quanlythuvien.data.remote

import com.example.quanlythuvien.data.model.request.LoginRequest
import com.example.quanlythuvien.data.model.request.RegisterRequest
import com.example.quanlythuvien.data.model.response.LoginResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ResponseBody>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}