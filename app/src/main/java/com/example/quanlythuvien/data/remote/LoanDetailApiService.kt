package com.example.quanlythuvien.data.remote

import com.example.quanlythuvien.data.model.request.LoanDetailRequest
import com.example.quanlythuvien.data.model.response.BookCopyResponse
import com.example.quanlythuvien.data.model.response.LoanDetailResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface LoanDetailApiService {
    @GET("api/loan-details/alerts/due-today")
    suspend fun getDueTodayAlerts(): Response<List<String>>

    // API Sửa (Cập nhật) chi tiết một cuốn sách trong phiếu mượn
    @PUT("api/loan-details/loan/{loanId}/copy/{copyId}")
    suspend fun updateLoanDetail(
        @Path("loanId") loanId: Long,
        @Path("copyId") copyId: Long,
        @Body request: LoanDetailRequest
    ): Response<LoanDetailResponse>

    // API Xóa một cuốn sách khỏi phiếu mượn
    // Spring Boot trả về ResponseEntity<Void> nên ở đây ta dùng Response<Unit>
    @DELETE("api/loan-details/loan/{loanId}/copy/{copyId}")
    suspend fun deleteLoanDetail(
        @Path("loanId") loanId: Long,
        @Path("copyId") copyId: Long
    ): Response<Unit>

    @GET("api/book-copies/available")
    suspend fun getAvailableCopies(): Response<List<BookCopyResponse>>
}