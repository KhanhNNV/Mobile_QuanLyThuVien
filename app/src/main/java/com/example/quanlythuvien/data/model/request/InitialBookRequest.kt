package com.example.quanlythuvien.data.model.request

data class InitialBookRequest(
    val title: String,
    val author: String,
    val isbn: String,
    val basePrice: Double,
    val categoryId: Long
)