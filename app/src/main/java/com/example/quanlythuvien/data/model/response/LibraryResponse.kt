package com.example.quanlythuvien.data.model.response

data class LibraryResponse(
    val libraryId: Long,
    val name: String,
    val address: String,
    val status: String,
    val maxLoansQuota: Int,
    val maxBooksQuota: Int
)