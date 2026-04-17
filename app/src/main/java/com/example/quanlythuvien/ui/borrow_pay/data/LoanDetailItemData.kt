package com.example.quanlythuvien.ui.borrow_pay.data

data class  LoanDetailItemData (
    val loanDetailId: Long,
    val bookId:Long,
    val title: String,
    val author: String,
    val categoryName: String,
    var returnDate: String?,
    var dueDate: String,           // Đổi thành var để gia hạn được
    var status: String

)