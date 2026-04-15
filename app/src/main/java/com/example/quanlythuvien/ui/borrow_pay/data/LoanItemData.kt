package com.example.quanlythuvien.ui.borrow_pay.data

data class LoanItemData(
    val loanId: Long,
    val borrowDate: String,
    var overallStatus: String,
    val readerName: String,
    val borrowedBooks: MutableList<LoanDetailItemData>
)