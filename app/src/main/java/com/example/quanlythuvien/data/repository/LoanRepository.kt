package com.example.quanlythuvien.data.repository

import com.example.quanlythuvien.data.remote.LoanApiService

class LoanRepository(private val apiService: LoanApiService) {
    suspend fun countBorrowingLoans() = apiService.countBorrowingLoans()
    suspend fun countOverdueLoans() = apiService.countOverdueLoans()
}