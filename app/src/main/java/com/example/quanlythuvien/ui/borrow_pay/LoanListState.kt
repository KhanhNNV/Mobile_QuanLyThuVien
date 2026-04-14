package com.example.quanlythuvien.ui.borrow_pay

// Chú ý: import class LoanItemData của bạn vào đây
import com.example.quanlythuvien.ui.borrow_pay.data.LoanItemData

sealed class LoanListState {
    object Idle : LoanListState()
    object Loading : LoanListState()

    // ĐÚNG CHUẨN: Thành công thì trả về cả một Danh sách (List)
    data class Success(val loans: List<LoanItemData>) : LoanListState()

    data class Error(val error: String) : LoanListState()
}