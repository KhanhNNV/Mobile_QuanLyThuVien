package com.example.quanlythuvien.data.model.response

import com.google.gson.annotations.SerializedName

data class BookCopyResponse(
    @SerializedName("copyId") val copyId: Long,
    @SerializedName("bookId") val bookId: Long,
    @SerializedName("title") val title: String?,
    @SerializedName("author") val author: String?,
    @SerializedName("barcode") val barcode: String?,
    @SerializedName("condition") val condition: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?
)