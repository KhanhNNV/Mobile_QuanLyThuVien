package com.example.quanlythuvien.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface LoanApiService {
    @GET("api/loans/library/{libraryId}/borrowing/count")
    suspend fun countBorrowingLoans(@Path("libraryId") libraryId: Long): Response<Long>

    @GET("api/dashboard/library/{libraryId}/overdue/count")
    suspend fun countOverdueLoans(@Path("libraryId") libraryId: Long): Response<Long>
}