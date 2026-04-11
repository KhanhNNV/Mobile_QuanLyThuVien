package com.example.quanlythuvien.data.remote

import com.example.quanlythuvien.data.model.request.InitialBookRequest
import com.example.quanlythuvien.data.model.response.InitialBookResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface BookApiService {
    @POST("api/books/welcome")
    suspend fun createInitialBook(@Body request: InitialBookRequest): Response<InitialBookResponse>
}