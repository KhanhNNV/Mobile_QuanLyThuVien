package com.example.quanlythuvien.data.model.request

data class BookCopyRequest(
    val bookId: Long,
    val barcode: String,
    val condition: String,
    val status: String
)
