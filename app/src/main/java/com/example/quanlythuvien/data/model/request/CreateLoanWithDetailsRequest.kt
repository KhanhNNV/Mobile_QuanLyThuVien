package com.example.quanlythuvien.data.model.request

import com.google.gson.annotations.SerializedName

data class CreateLoanWithDetailsRequest(
    @SerializedName("readerId")
    val readerId: Long,

    @SerializedName("copyIds")
    val copyIds: List<Long>
)