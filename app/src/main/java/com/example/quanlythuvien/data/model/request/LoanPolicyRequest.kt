package com.example.quanlythuvien.data.model.request

data class LoanPolicyRequest(
    val categoryId: Long?,
    val applyForStudent: Boolean,
    val maxBorrowDays: Int
)