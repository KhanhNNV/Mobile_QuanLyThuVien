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

                        // --- Xử lý danh sách sách mượn (Bây giờ dùng bookDetails thay vì bookTitles) ---
                        val detailList = dto.bookDetails?.map { detail ->
                            LoanDetailItemData(
                                bookId = detail.copyId ?: 0L,           // Lấy ID thật từ backend
                                title = detail.title ?: "Không rõ",     // Tên sách
                                author = detail.author ?: "Không rõ",   // Tác giả
                                categoryName = detail.category ?: "Không rõ", // Thể loại
                                returnDate = detail.returnDate,         // Ngày trả
                                dueDate = detail.dueDate ?: "Chưa có",  // Hạn trả
                                status = detail.status ?: dto.status    // Lấy trạng thái của từng cuốn sách
                            )
                        }?.toMutableList() ?: mutableListOf()

                        // --- Tạo Object cha ---
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