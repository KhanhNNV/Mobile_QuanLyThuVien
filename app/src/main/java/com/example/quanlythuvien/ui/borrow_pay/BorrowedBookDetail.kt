package com.example.quanlythuvien.ui.borrow_pay

data class BorrowedBookDetail(
    val title: String,
    val author: String,
    val categoryName: String,
    var returnDate: String?,       // Đổi thành var để gán ngày trả khi bấm nút
    var status: String             // Đổi thành var để thay đổi Mượn/Trả/Mất
)