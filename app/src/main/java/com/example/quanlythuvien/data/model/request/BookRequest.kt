package com.example.quanlythuvien.data.model.request

data class BookRequest(
    val libraryId: Long,
    val categoryId: Long,
    val isbn: String,
    val title: String,
    val author: String,
    val basePrice: Double
)