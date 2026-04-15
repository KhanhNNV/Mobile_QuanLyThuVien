package com.example.quanlythuvien.ui.loan_detail

import com.example.quanlythuvien.data.model.response.LoanResponse

sealed class LoanDetailState {
    object Idle : LoanDetailState()
    object Loading : LoanDetailState()

    // Thành công khi tải dữ liệu chi tiết phiếu
    data class Success(val loan: LoanResponse) : LoanDetailState()

    // Lỗi chung (API lỗi, mất mạng)
    data class Error(val message: String) : LoanDetailState()

    // Trạng thái riêng cho hành động Xóa cả phiếu mượn
    object DeleteLoanSuccess : LoanDetailState()

    // Trạng thái riêng cho hành động Sửa/Xóa từng cuốn sách trong phiếu
    object UpdateBookSuccess : LoanDetailState()
}