package com.example.quanlythuvien.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface LoanDetailApiService {
    @GET("api/loan-details/library/{libraryId}/alerts/due-today")
    suspend fun getDueTodayAlerts(@Path("libraryId") libraryId: Long): Response<List<String>>
}