package com.example.quanlythuvien.data.model.response

import com.google.gson.annotations.SerializedName

data class FeeInvoiceResponse(
    @SerializedName("invoiceId")
    val invoiceId: Long,

    @SerializedName("libraryId")
    val libraryId: Long,

    @SerializedName("readerId")
    val readerId: Long,

    @SerializedName("loanId")
    val loanId: Long?,

    @SerializedName("type")
    val type: String,

    @SerializedName("totalAmount")
    val totalAmount: Double,

    @SerializedName("status")
    val status: String,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("updateAt")
    val updateAt: String?,

    @SerializedName("readerName")
    val readerName: String,

    @SerializedName("paymentMethod")
    val paymentMethod: String? = null,

    @SerializedName("description")
    val description: String? = null
) {
    // Các trường tính toán để tương thích với code cũ
    val invoiceType: String
        get() = type

    val amount: Double
        get() = totalAmount

    val updatedAt: String?
        get() = updateAt
}