package com.example.quanlythuvien.data.repository

import com.example.quanlythuvien.data.model.request.LoanDetailRequest
import com.example.quanlythuvien.data.remote.LoanDetailApiService

class LoanDetailRepository(private val apiService: LoanDetailApiService) {
    suspend fun getDueTodayAlerts() = apiService.getDueTodayAlerts()

    // Cập nhật thông tin chi tiết một cuốn sách (Ngày trả, trạng thái, tiền phạt...)
    suspend fun updateLoanDetail(
        loanId: Long,
        copyId: Long,
        request: LoanDetailRequest
    ) = apiService.updateLoanDetail(loanId, copyId, request)

    // Xóa một cuốn sách cụ thể khỏi phiếu mượn
    suspend fun deleteLoanDetail(
        loanId: Long,
        copyId: Long
    ) = apiService.deleteLoanDetail(loanId, copyId)

    suspend fun getAvailableCopies() = apiService.getAvailableCopies()
}