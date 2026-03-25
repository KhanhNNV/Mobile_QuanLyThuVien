package com.example.quanlythuvien.ui.borrow_pay

data class BorrowPayItem(
    val loanId: Long,
    val borrowDate: String,
    var dueDate: String,           // Đổi thành var để gia hạn được
    var overallStatus: String,     // Đổi thành var để cập nhật trạng thái tổng
    val readerId: String,
    val readerName: String,
    val borrowedBooks: List<BorrowedBookDetail>
)