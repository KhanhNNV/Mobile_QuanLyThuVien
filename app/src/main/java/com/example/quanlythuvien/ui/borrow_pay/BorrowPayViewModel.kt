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

                    // MAP DỮ LIỆU: Từ DTO của Backend sang LoanItemData của bạn
                    val uiModelList = loanResponseList.map { dto ->

                        // --- Xử lý danh sách sách mượn (Khớp 100% với file Data của bạn) ---
                        val detailList = dto.bookTitles?.map { titleName ->
                            LoanDetailItemData(
                                bookId = 0L,              // Gán mặc định vì API List không có
                                title = titleName,        // Lấy tên sách từ danh sách trả về
                                author = "N/A",           // Gán mặc định
                                categoryName = "N/A",     // Gán mặc định
                                returnDate = null,        // Gán mặc định
                                dueDate = "N/A",          // Gán mặc định
                                status = dto.status       // Dùng trạng thái chung của phiếu mượn
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