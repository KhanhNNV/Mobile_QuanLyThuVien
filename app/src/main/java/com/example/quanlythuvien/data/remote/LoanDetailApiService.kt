package com.example.quanlythuvien.data.remote

import com.example.quanlythuvien.data.model.request.LoanDetailRequest
import com.example.quanlythuvien.data.model.request.UpdateLoanDetailRequest
import com.example.quanlythuvien.data.model.response.BookCopyResponse
import com.example.quanlythuvien.data.model.response.LoanDetailResponse
import com.example.quanlythuvien.data.model.response.PageResponse
import retrofit2.Response
import retrofit2.http.*

interface LoanDetailApiService {
    @GET("api/loan-details/alerts/due-today")
    suspend fun getDueTodayAlerts(): Response<List<String>>

    // Thêm sách mới vào phiếu mượn
    @POST("api/loan-details")
    suspend fun createLoanDetail(@Body request: LoanDetailRequest): Response<LoanDetailResponse>

    // Trả sách: Thay đổi hoàn toàn URL và Param (Sử dụng loanDetailId)
    @PUT("api/loan-details/{loanDetailId}/return")
    suspend fun returnBook(
        @Path("loanDetailId") loanDetailId: Long,
        @Query("condition") condition: String? // Truyền enum ConditionBookCopy dưới dạng String
    ): Response<LoanDetailResponse>

    // Xóa sách khỏi phiếu (Sử dụng loanDetailId)
    @DELETE("api/loan-details/{loanDetailId}")
    suspend fun deleteLoanDetail(@Path("loanDetailId") loanDetailId: Long): Response<Unit>

    @GET("api/book-copies/available")
    suspend fun getAvailableCopies(): Response<List<BookCopyResponse>>

    @PUT("api/loan-details/{loanDetailId}/admin-update")
    suspend fun updateLoanDetailAdmin(
        @Path("loanDetailId") loanDetailId: Long,
        @Body request: UpdateLoanDetailRequest
    ): Response<LoanDetailResponse>

    //Show phân trang book cho reader
    @GET("api/loan-details/reader/{readerId}")
    suspend fun getReaderLoans(
        @Path("readerId") readerId: Long,
        @Query("status") status: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<PageResponse<LoanDetailResponse>>
}