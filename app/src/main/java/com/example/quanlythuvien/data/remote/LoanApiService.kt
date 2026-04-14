package com.example.quanlythuvien.data.remote

import com.example.quanlythuvien.data.model.response.LoanResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface LoanApiService {
    @GET("api/loans/borrowing/count")
    suspend fun countBorrowingLoans(): Response<Long>

    @GET("api/loans/overdue/count")
    suspend fun countOverdueLoans(): Response<Long>

    @GET("api/loans/filter")
    suspend fun getFilteredLoans(
        @Query("status") status: String? = null,
        @Query("fromDate") fromDate: String? = null,
        @Query("toDate") toDate: String? = null,
        @Query("search") search: String? = null
    ): Response<List<LoanResponse>>
}