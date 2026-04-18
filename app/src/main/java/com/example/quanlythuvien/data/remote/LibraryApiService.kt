package com.example.quanlythuvien.data.remote

import com.example.quanlythuvien.data.model.request.LibraryRequest
import com.example.quanlythuvien.data.model.response.LibraryResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface LibraryApiService {

    @PUT("api/libraries")
    suspend fun updateLibrary(@Body request: LibraryRequest): Response<LibraryResponse>

    @GET("api/libraries")
    suspend fun getLibraryById(): Response<LibraryResponse>

    @PUT("api/libraries/loansQuota")
    suspend fun updateLibraryLoansQuota(@Query("maxLoansQuota") quota: Int): Response<LibraryResponse>

    @PUT("api/libraries/booksQuota")
    suspend fun updateLibraryBooksQuota(@Query("maxBookssQuota") quota: Int): Response<LibraryResponse>
}