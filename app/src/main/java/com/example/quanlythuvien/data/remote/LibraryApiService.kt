package com.example.quanlythuvien.data.remote

import com.example.quanlythuvien.data.model.response.LibraryConfigResponse
import retrofit2.Response
import retrofit2.http.GET

interface LibraryApiService {
    @GET("api/libraries/config")
    suspend fun getLibraryConfig(): Response<LibraryConfigResponse>
}