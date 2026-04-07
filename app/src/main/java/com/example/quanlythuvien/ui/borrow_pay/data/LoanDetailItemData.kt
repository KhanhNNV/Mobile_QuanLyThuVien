package com.example.quanlythuvien.ui.borrow_pay.data

data class  LoanDetailItemData (
    val bookId:Long,
    val title: String,
    val author: String,
    val categoryName: String,
    var returnDate: String?,
    var status: String
)