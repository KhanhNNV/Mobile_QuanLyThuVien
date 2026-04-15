package com.example.quanlythuvien.data.model.request

data class ReaderRequest(
    val fullName: String,
    val phone: String,
    val barcode: String,
    val libraryId: Long,
    val membershipExpiry: String? = null
)