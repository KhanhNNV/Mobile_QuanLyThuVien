package com.example.quanlythuvien.data.model.request

data class ViolationRequest(
    val readerId: Long,
    val libraryId: Long,
    val reason: String,
    val loanId: Long? = null,
    val loanDetailId: Long? = null
)