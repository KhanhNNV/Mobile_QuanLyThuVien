package com.example.quanlythuvien.data.model.request

import com.google.gson.annotations.SerializedName

data class LoanDetailRequest(
    @SerializedName("loanId") val loanId: Long,
    @SerializedName("copyId") val copyId: Long,
    @SerializedName("borrowDays") val borrowDays: Int // ĐỔI TỪ dueDate (String) sang borrowDays (Int)
)