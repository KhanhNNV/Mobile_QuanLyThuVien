package com.example.quanlythuvien.data.model.response

import com.google.gson.annotations.SerializedName

data class LoanResponse(
    @SerializedName("loanId") val loanId: Long,
    @SerializedName("libraryName") val libraryName: String,
    @SerializedName("readerName") val readerName: String,
    @SerializedName("processorName") val processorName: String?,
    @SerializedName("borrowDate") val borrowDate: String,
    @SerializedName("status") val status: String,

    // ĐỔI TÊN: Từ bookDetails thành loanDetails, đổi kiểu dữ liệu thành LoanDetailResponse
    @SerializedName("loanDetails") val loanDetails: List<LoanDetailResponse>?,

    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updateAt") val updateAt: String
)