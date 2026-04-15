package com.example.quanlythuvien.data.model.response

data class BookResponse(
    val bookId: Long,
    val libraryId: Long? = null,
    val isbn: String,
    val title: String,
    val author: String,
    val basePrice: Double? = null,
    val availableCopies: Int? = null,
    val categoryId: Long? = null,
    val categoryName: String? = null
)