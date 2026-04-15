package com.example.quanlythuvien.data.model.response

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime


data class BookDetailInfoDto(
    @SerializedName("copyId")
    val copyId: Long,

    @SerializedName("title")
    val title: String?, // Nên để nullable đề phòng DB bị null

    @SerializedName("author")
    val author: String?,

    @SerializedName("category")
    val category: String?,

    @SerializedName("dueDate")
    val dueDate: String?,

    @SerializedName("returnDate")
    val returnDate: String?,

    @SerializedName("status")
    val status: String?
)