package com.example.quanlythuvien.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface LoanDetailApiService {
    @GET("api/loan-details/alerts/due-today")
    suspend fun getDueTodayAlerts(): Response<List<String>>
}