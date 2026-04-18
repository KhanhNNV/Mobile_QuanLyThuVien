package com.example.quanlythuvien.data.model.response


import com.google.gson.annotations.SerializedName

data class ViolationResponse(
    @SerializedName("violationId")
    val violationId: Long,

    @SerializedName("readerId")
    val readerId: Long,

    @SerializedName("readerName")
    val readerName: String,

    @SerializedName("barcode")
    val barcode: String,

    @SerializedName("reason")
    val reason: String,

    @SerializedName("status")
    val status: String,

    val loanDetailId: Long?,
    val loanId: Long?,

    @SerializedName("bookTitle")
    val bookTitle: String?,

    @SerializedName("createdAt")
    val createdAt: String,

    val updatedAt: String?
)