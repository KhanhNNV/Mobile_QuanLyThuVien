package com.example.quanlythuvien.data.repository

import com.example.quanlythuvien.data.remote.LoanApiService

class LoanRepository(private val apiService: LoanApiService) {
    suspend fun countBorrowingLoans(libraryId: Long) = apiService.countBorrowingLoans(libraryId)
    suspend fun countOverdueLoans(libraryId: Long) = apiService.countOverdueLoans(libraryId)
}