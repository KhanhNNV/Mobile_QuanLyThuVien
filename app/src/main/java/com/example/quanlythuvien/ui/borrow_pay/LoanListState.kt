package com.example.quanlythuvien.ui.borrow_pay

import com.example.quanlythuvien.ui.borrow_pay.data.LoanItemData

sealed class LoanListState {
    object Idle : LoanListState()
    object Loading : LoanListState()

    data class Success(val loans: List<LoanItemData>) : LoanListState()

    data class Error(val error: String) : LoanListState()
}