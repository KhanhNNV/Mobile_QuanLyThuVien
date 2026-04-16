package com.example.quanlythuvien.ui.borrow_pay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quanlythuvien.data.repository.LoanRepository
import com.example.quanlythuvien.ui.borrow_pay.data.LoanDetailItemData
import com.example.quanlythuvien.ui.borrow_pay.data.LoanItemData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BorrowPayViewModel(private val repository: LoanRepository) : ViewModel() {

    private val _loanListState = MutableStateFlow<LoanListState>(LoanListState.Idle)
    val loanListState: StateFlow<LoanListState> = _loanListState

    fun fetchLoans(
        status: String? = null,
        fromDate: String? = null,
        toDate: String? = null,
        search: String? = null
    ) {
        viewModelScope.launch {
            _loanListState.value = LoanListState.Loading

            try {
                val response = repository.getFilteredLoans(status, fromDate, toDate, search)

                if (response.isSuccessful && response.body() != null) {
                    val loanResponseList = response.body()!!

                    // MAP DỮ LIỆU: Từ DTO của Backend sang LoanItemData
                    val uiModelList = loanResponseList.map { dto ->

                        // Lặp qua loanDetails thay vì bookDetails
                        val detailList = dto.loanDetails?.map { detail ->
                            LoanDetailItemData(
                                loanDetailId = detail.loanDetailId, // Gán ID mới
                                bookId = detail.bookId ?: 0L,
                                title = detail.bookTitle ?: "Không rõ",
                                author = "Không rõ", // Backend chưa trả về, tạm gán
                                categoryName = "Không rõ", // Backend chưa trả về, tạm gán
                                returnDate = detail.returnDate,
                                dueDate = detail.dueDate ?: "Chưa có",
                                status = detail.status
                            )
                        }?.toMutableList() ?: mutableListOf()

                        LoanItemData(
                            loanId = dto.loanId,
                            borrowDate = dto.borrowDate,
                            overallStatus = dto.status,
                            readerName = dto.readerName,
                            borrowedBooks = detailList
                        )
                    }

                    _loanListState.value = LoanListState.Success(uiModelList)

                } else {
                    _loanListState.value = LoanListState.Error("Lỗi máy chủ: Mã ${response.code()}")
                }
            } catch (e: Exception) {
                _loanListState.value = LoanListState.Error("Lỗi kết nối mạng: ${e.localizedMessage}")
            }
        }
    }
}