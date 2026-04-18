package com.example.quanlythuvien.data.remote

import retrofit2.Response
import retrofit2.http.GET

interface ViolationApiService {
    @GET("api/violations/alert")
    suspend fun getViolationQuantityAlerts(): Response<List<String>>
}