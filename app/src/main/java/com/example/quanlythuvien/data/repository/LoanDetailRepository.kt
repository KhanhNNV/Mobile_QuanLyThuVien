package com.example.quanlythuvien.data.repository

import com.example.quanlythuvien.data.model.request.LoanDetailRequest
import com.example.quanlythuvien.data.model.request.UpdateLoanDetailRequest
import com.example.quanlythuvien.data.model.response.LoanDetailResponse
import com.example.quanlythuvien.data.remote.LoanDetailApiService
import retrofit2.Response
class LoanDetailRepository(private val apiService: LoanDetailApiService) {

    suspend fun getDueTodayAlerts() = apiService.getDueTodayAlerts()

    /**
     * Nghiệp vụ trả sách: Thay thế cho hàm update cũ.
     * @param loanDetailId: ID duy nhất của dòng chi tiết mượn.
     * @param condition: Tình trạng sách khi trả (NEW, GOOD, FAIR, POOR).
     */
    suspend fun returnBook(
        loanDetailId: Long,
        condition: String? = null
    ) = apiService.returnBook(loanDetailId, condition)

    /**
     * Xóa một cuốn sách cụ thể khỏi phiếu mượn.
     * Sử dụng loanDetailId duy nhất theo thiết kế Backend mới.
     */
    suspend fun deleteLoanDetail(loanDetailId: Long) =
        apiService.deleteLoanDetail(loanDetailId)

    suspend fun getAvailableCopies() = apiService.getAvailableCopies()

    // Thêm hàm tạo mới chi tiết mượn nếu bạn cần dùng
    suspend fun createLoanDetail(request: LoanDetailRequest) =
        apiService.createLoanDetail(request)

    suspend fun updateLoanDetailAdmin(
        loanDetailId: Long,
        request: UpdateLoanDetailRequest
    ): Response<LoanDetailResponse> {
        return apiService.updateLoanDetailAdmin(loanDetailId, request)
    }
}