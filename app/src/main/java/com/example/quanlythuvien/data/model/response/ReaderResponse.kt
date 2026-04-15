package com.example.quanlythuvien.data.model.response

data class ReaderResponse(
    val readerId: Long,
    val fullName: String,
    val phone: String,
    val barcode: String,
    val isBlocked: Boolean,
    val membershipExpiry: String?,
    val createdAt: String?,
    val updatedAt: String?
)