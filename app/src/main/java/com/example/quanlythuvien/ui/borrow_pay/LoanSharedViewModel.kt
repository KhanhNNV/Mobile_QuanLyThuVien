package com.example.quanlythuvien.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.quanlythuvien.ui.borrow_pay.data.LoanItemData


//Clase này đóng vai trò là "người vận chuyển" giữ dữ liệu khi bạn chuyển qua lại giữa 2 màn hình.
//Ở đây là (BorrowPay và Loan)
class LoanSharedViewModel : ViewModel() {
    var masterLoanList: MutableList<LoanItemData> = mutableListOf()
    // Lưu trữ phiếu mượn được click để truyền sang LoanFragment
    val selectedLoanToView = MutableLiveData<LoanItemData>()

    // Lưu trữ phiếu mượn sau khi đã bị chỉnh sửa để truyền ngược về BorrowPayFragment
    val updatedLoanToSave = MutableLiveData<LoanItemData?>()

    // Thêm biến này để báo hiệu xóa
    val deletedLoanId = MutableLiveData<Long?>()

    // Hàm khởi tạo dữ liệu lần đầu duy nhất
    fun loadData(data: List<LoanItemData>) {
        if (masterLoanList.isEmpty()) {
            masterLoanList = data.toMutableList()
        }
    }


}