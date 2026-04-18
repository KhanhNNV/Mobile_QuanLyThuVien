package com.example.quanlythuvien.ui.borrow_pay.data

import com.example.quanlythuvien.data.model.response.ReaderResponse

data class ReaderDropDownItem(val reader: ReaderResponse) {
    override fun toString(): String {
        // Chuỗi này sẽ hiện lên UI và dùng để search (Search theo cả mã và tên)
        return "${reader.barcode} - ${reader.fullName}"
    }
}