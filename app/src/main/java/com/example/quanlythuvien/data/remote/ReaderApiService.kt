package com.example.quanlythuvien.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ReaderApiService {
    @GET("api/readers/library/{libraryId}/count")
    suspend fun countReaders(@Path("libraryId") libraryId: Long): Response<Long>
}