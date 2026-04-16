package com.example.quanlythuvien.data.repository

import com.example.quanlythuvien.data.remote.LoanApiService

class LoanRepository(private val apiService: LoanApiService) {

    suspend fun countBorrowingLoans() = apiService.countBorrowingLoans()

    suspend fun countOverdueLoans() = apiService.countOverdueLoans()

    // Lọc danh sách phiếu mượn với các tham số từ ViewModel
    suspend fun getFilteredLoans(
        status: String? = null,
        fromDate: String? = null,
        toDate: String? = null,
        search: String? = null
    ) = apiService.getFilteredLoans(status, fromDate, toDate, search)

    // Lấy chi tiết một phiếu mượn theo ID
    suspend fun getLoanById(id: Long) = apiService.getLoanById(id)

    // Xóa một phiếu mượn
    suspend fun deleteLoan(id: Long) = apiService.deleteLoan(id)
}