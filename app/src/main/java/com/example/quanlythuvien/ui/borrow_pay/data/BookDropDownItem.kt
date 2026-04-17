package com.example.quanlythuvien.ui.borrow_pay.data

import com.example.quanlythuvien.data.model.response.BookCopyResponse

data class BookDropDownItem(val bookCopy: BookCopyResponse) {
    override fun toString(): String {
        return "${bookCopy.barcode} - ${bookCopy.title}"
    }
}