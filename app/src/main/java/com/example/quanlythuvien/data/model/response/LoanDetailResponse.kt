package com.example.quanlythuvien.data.model.response

import com.google.gson.annotations.SerializedName

data class LoanDetailResponse(
    @SerializedName("loanId")
    val loanId: Long,

    @SerializedName("copyId")
    val copyId: Long,

    @SerializedName("bookTitle")
    val bookTitle: String,

    @SerializedName("dueDate")
    val dueDate: String?, // Ngày hết hạn (có thể null)

    @SerializedName("returnDate")
    val returnDate: String?, // Ngày trả thực tế (sẽ null nếu chưa trả)

    @SerializedName("status")
    val status: String, // Enum StatusLoanDetail dạng String (VD: "BORROWING", "LOST")

    @SerializedName("penaltyAmount")
    val penaltyAmount: Double?, // Tiền phạt (có thể null hoặc 0.0)

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("updateAt")
    val updateAt: String
)