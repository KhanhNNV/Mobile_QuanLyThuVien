package com.example.quanlythuvien.data.model.response

import java.math.BigDecimal

data class ReaderResponse(
    val readerId: Long,
    val fullName: String,
    val phone: String,
    val barcode: String,
    val isBlocked: Boolean,
    val membershipExpiry: String?,
    val createdAt: String?,
    val totalBorrowedBooks: Int,
    val totalReturnBook:Int,
    val totalOverdueBooks: Int,
    val  totalDebt: BigDecimal?
)