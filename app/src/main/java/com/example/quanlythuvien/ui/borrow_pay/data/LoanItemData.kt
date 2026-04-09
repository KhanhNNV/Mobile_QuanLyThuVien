package com.example.quanlythuvien.ui.borrow_pay.data

data class LoanItemData(
    val loanId: Long,
    val borrowDate: String,
    var overallStatus: String,     // Đổi thành var để cập nhật trạng thái tổng
    val readerName: String,
    val borrowedBooks: MutableList<LoanDetailItemData>
)