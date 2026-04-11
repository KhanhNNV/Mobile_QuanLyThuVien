package com.example.quanlythuvien.data.model.response

data class InitialBookResponse(
    val bookId: Long,
    val title: String,
    val author: String,
    val isbn: String,
    val basePrice: Double,
    val libraryId: Long,
    val categoryId: Long,
    val copyId: Long,
    val barcode: String,
    val condition: String,
    val status: String
)