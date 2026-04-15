package com.example.quanlythuvien.data.model.request

data class LoanDetailRequest(
    val loanId: Long,
    val copyId: Long,

    // Sử dụng String cho Ngày tháng để Retrofit dễ dàng gửi lên server
    // dưới định dạng "yyyy-MM-dd'T'HH:mm:ss"
    val dueDate: String,

    // returnDate có thể null vì nếu sách đang mượn thì chưa có ngày trả
    val returnDate: String? = null,

    // Thay vì dùng Enum StatusLoanDetail, ta dùng String như đã chốt ở các bước trước
    val status: String,

    // penaltyAmount có thể null nếu không có phạt
    val penaltyAmount: Double? = null
)