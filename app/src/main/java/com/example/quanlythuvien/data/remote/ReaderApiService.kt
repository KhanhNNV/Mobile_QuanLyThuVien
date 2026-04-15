package com.example.quanlythuvien.data.remote

import com.example.quanlythuvien.data.model.request.ReaderRequest
import com.example.quanlythuvien.data.model.response.ReaderResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path


interface ReaderApiService {
    @GET("api/readers/count")
    suspend fun countReaders(): Response<Long>

    @POST("api/readers")
    suspend fun createReader(@Body request: ReaderRequest): Response<ReaderResponse>
}