package com.example.quanlythuvien.data.model.response

data class BookCopyResponse(
    val copyId: Long,
    val bookId: Long,
    val barcode: String,
    val condition: String,
    val status: String
)
