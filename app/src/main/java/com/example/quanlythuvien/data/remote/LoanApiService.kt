package com.example.quanlythuvien.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface LoanApiService {
    @GET("api/loans/borrowing/count")
    suspend fun countBorrowingLoans(): Response<Long>

    @GET("api/loans/overdue/count")
    suspend fun countOverdueLoans(): Response<Long>
}