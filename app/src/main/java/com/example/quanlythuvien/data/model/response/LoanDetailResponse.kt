package com.example.quanlythuvien.data.model.response

import com.google.gson.annotations.SerializedName

data class LoanDetailResponse(
    @SerializedName("loanDetailId") val loanDetailId: Long, // QUAN TRỌNG: ID mới để gọi API
    @SerializedName("loanId") val loanId: Long,
    @SerializedName("copyId") val copyId: Long,
    @SerializedName("bookId") val bookId: Long?,
    @SerializedName("bookTitle") val bookTitle: String?,
    @SerializedName("author") val author: String?,
    @SerializedName("category") val category: String?,
    @SerializedName("dueDate") val dueDate: String?,
    @SerializedName("returnDate") val returnDate: String?,
    @SerializedName("status") val status: String,
    @SerializedName("penaltyAmount") val penaltyAmount: Double?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updateAt") val updateAt: String?
)