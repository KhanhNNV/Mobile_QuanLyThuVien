package com.example.quanlythuvien.data.repository

import com.example.quanlythuvien.data.remote.LoanApiService

class LoanRepository(private val apiService: LoanApiService) {
    suspend fun countBorrowingLoans() = apiService.countBorrowingLoans()
    suspend fun countOverdueLoans() = apiService.countOverdueLoans()

    // Phải khai báo các tham số hứng dữ liệu từ ViewModel
    suspend fun getFilteredLoans(
        status: String? = null,
        fromDate: String? = null,
        toDate: String? = null,
        search: String? = null
    ) = apiService.getFilteredLoans(status, fromDate, toDate, search)
}