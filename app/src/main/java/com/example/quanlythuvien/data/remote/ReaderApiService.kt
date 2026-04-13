package com.example.quanlythuvien.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ReaderApiService {
    @GET("api/readers/count")
    suspend fun countReaders(): Response<Long>
}