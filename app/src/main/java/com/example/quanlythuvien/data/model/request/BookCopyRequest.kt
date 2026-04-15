package com.example.quanlythuvien.data.model.request

data class BookCopyRequest(
    val bookId: Long? = null,
    val barcode: String? = null,
    val condition: String? = null,
    val status: String? = null
)
