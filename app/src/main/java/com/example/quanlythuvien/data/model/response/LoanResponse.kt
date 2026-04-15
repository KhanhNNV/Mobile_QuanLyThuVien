package com.example.quanlythuvien.data.model.response

import com.google.gson.annotations.SerializedName

data class LoanResponse(
    @SerializedName("loanId")
    val loanId: Long,

    @SerializedName("libraryName")
    val libraryName: String,

    @SerializedName("readerName")
    val readerName: String,

    @SerializedName("processorName")
    val processorName: String?, // Có thể null nếu chưa ai xử lý

    @SerializedName("borrowDate")
    val borrowDate: String, // Chuỗi định dạng "yyyy-MM-dd HH:mm:ss"

    @SerializedName("status")
    val status: String, // Nhận Enum StatusLoan dưới dạng String (VD: "BORROWING", "RETURNED")

    @SerializedName("bookTitles")
    val bookTitles: List<String>?, // Danh sách tên sách

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("updateAt")
    val updateAt: String
)