package com.example.quanlythuvien.data.model.response

data class ReaderResponse (
    val readerId: Long,

    val fullName: String,

    val phone: String,

    val barcode: String?,


    val isBlocked: Boolean,

    val createdAt: String?,

    val membershipExpiry: String?
)