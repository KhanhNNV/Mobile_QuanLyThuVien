package com.example.quanlythuvien.ui.borrow_pay.data

data class LoanPolicy(
    val policyId: String,
    val categoryName: String,
    val maxDays: Int,
    val targetCustomer: String
)