package com.example.quanlythuvien.ui.borrow_pay.data

data class CreateLoanUiState(
    val isLoading: Boolean = false,
    val readers: List<ReaderDropDownItem> = emptyList(),
    val availableBooks: List<BookDropDownItem> = emptyList(),

    val selectedReader: ReaderDropDownItem? = null,
    val selectedBooksForLoan: List<BookDropDownItem> = emptyList(),

    val error: String? = null,
    val violationMessages: List<String> = emptyList(), // Danh sách lý do vi phạm
    val isCreateSuccess: Boolean = false
)