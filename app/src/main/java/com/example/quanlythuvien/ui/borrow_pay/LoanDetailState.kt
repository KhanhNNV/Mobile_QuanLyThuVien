package com.example.quanlythuvien.ui.loan_detail

import com.example.quanlythuvien.data.model.response.LoanDetailResponse

sealed class LoanDetailState {
    object Idle : LoanDetailState()
    object Loading : LoanDetailState()

    // ĐÚNG CHUẨN: Thành công thì trả về 1 Object trọn vẹn
    data class Success(val loanDetail: LoanDetailResponse) : LoanDetailState()

    data class Error(val error: String) : LoanDetailState()
}