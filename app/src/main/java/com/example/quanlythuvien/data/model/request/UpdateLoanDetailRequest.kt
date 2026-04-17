package com.example.quanlythuvien.data.model.request

import com.google.gson.annotations.SerializedName

data class UpdateLoanDetailRequest(
    @SerializedName("copyId") val copyId: Long,
    @SerializedName("status") val status: String,
    @SerializedName("dueDate") val dueDate: String?,
    // THÊM DÒNG NÀY ĐỂ GỬI TÌNH TRẠNG SÁCH (NEW, GOOD, FAIR, POOR)
    @SerializedName("condition") val condition: String? = null
)