package com.example.quanlythuvien.data.model.response

data class LoanPolicyResponse(
    val policyId: Long,
    val categoryId: Long?,
    val categoryName: String,
    val applyForStudent: Boolean,
    val maxBorrowDays: Int
)