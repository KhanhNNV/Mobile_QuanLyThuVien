package com.example.quanlythuvien.ui.loan_detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quanlythuvien.data.model.request.UpdateLoanDetailRequest
import com.example.quanlythuvien.data.repository.LoanDetailRepository
import com.example.quanlythuvien.data.repository.LoanRepository
import com.example.quanlythuvien.ui.borrow_pay.data.BookData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoanDetailViewModel(
    private val loanRepo: LoanRepository,
    private val detailRepo: LoanDetailRepository
) : ViewModel() {

    private val _state = MutableStateFlow<LoanDetailState>(LoanDetailState.Idle)
    val state: StateFlow<LoanDetailState> = _state

    private val _availableBooks = MutableStateFlow<List<BookData>>(emptyList())
    val availableBooks: StateFlow<List<BookData>> = _availableBooks

    // Lấy danh sách sách có sẵn cho Spinner
    fun fetchAvailableBooks() {
        viewModelScope.launch {
            try {
                val response = detailRepo.getAvailableCopies()

                if (response.isSuccessful && response.body() != null) {
                    val bookDataList = response.body()!!.map { dto ->
                        BookData(
                            copyId = dto.copyId,
                            title = dto.title ?: "Không rõ",
                            author = dto.author ?: "Không rõ",
                            barcode = dto.barcode ?: ""
                        )
                    }
                    _availableBooks.value = bookDataList
                    Log.d("DEBUG_SPINNER", "Thành công! Lấy được ${bookDataList.size} cuốn sách")
                } else {
                    Log.e("DEBUG_SPINNER", "Lỗi API: Code ${response.code()} - ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("DEBUG_SPINNER", "Lỗi Exception: ${e.message}")
            }
        }
    }

    // 1. Lấy chi tiết phiếu mượn từ Server bằng ID
    fun fetchLoanById(id: Long) {
        viewModelScope.launch {
            _state.value = LoanDetailState.Loading
            try {
                val response = loanRepo.getLoanById(id)
                if (response.isSuccessful && response.body() != null) {
                    _state.value = LoanDetailState.Success(response.body()!!)
                } else {
                    _state.value = LoanDetailState.Error("Không thể tải thông tin phiếu mượn")
                }
            } catch (e: Exception) {
                _state.value = LoanDetailState.Error("Lỗi kết nối: ${e.localizedMessage}")
            }
        }
    }
    // Đổi tham số request thành UpdateLoanDetailRequest và dùng hàm API mới
    fun updateBookInLoan(loanDetailId: Long, request: UpdateLoanDetailRequest, parentLoanId: Long) {
        viewModelScope.launch {
            try {
                val response = detailRepo.updateLoanDetailAdmin(loanDetailId, request)
                if (response.isSuccessful) {
                    _state.value = LoanDetailState.UpdateBookSuccess
                    fetchLoanById(parentLoanId)
                } else {
                    _state.value = LoanDetailState.Error("Không thể ghi đè thông tin sách")
                }
            } catch (e: Exception) {
                _state.value = LoanDetailState.Error("Lỗi kết nối khi sửa sách")
            }
        }
    }

    // 2. Xóa toàn bộ phiếu mượn
    fun deleteWholeLoan(id: Long) {
        viewModelScope.launch {
            try {
                val response = loanRepo.deleteLoan(id)
                if (response.isSuccessful) {
                    _state.value = LoanDetailState.DeleteLoanSuccess
                } else {
                    _state.value = LoanDetailState.Error("Xóa phiếu mượn thất bại")
                }
            } catch (e: Exception) {
                _state.value = LoanDetailState.Error("Lỗi hệ thống: ${e.localizedMessage}")
            }
        }
    }

    // 3. TRẢ SÁCH (Dùng loanDetailId)
    // Cần truyền parentLoanId để sau khi gọi API xong, nó tự động tải lại chi tiết Phiếu mượn
    fun returnBook(loanDetailId: Long, condition: String?, parentLoanId: Long) {
        viewModelScope.launch {
            try {
                val response = detailRepo.returnBook(loanDetailId, condition)
                if (response.isSuccessful) {
                    _state.value = LoanDetailState.UpdateBookSuccess
                    // Tự động load lại dữ liệu để UI cập nhật trạng thái "Đã trả"
                    fetchLoanById(parentLoanId)
                } else {
                    _state.value = LoanDetailState.Error("Không thể trả sách")
                }
            } catch (e: Exception) {
                _state.value = LoanDetailState.Error("Lỗi kết nối khi trả sách")
            }
        }
    }

    // 4. XÓA MỘT CUỐN SÁCH KHỎI PHIẾU (Dùng loanDetailId)
    fun deleteBookFromLoan(loanDetailId: Long, parentLoanId: Long) {
        viewModelScope.launch {
            try {
                val response = detailRepo.deleteLoanDetail(loanDetailId)
                if (response.isSuccessful) {
                    _state.value = LoanDetailState.UpdateBookSuccess
                    // Load lại dữ liệu để UI biến mất cuốn sách vừa xóa
                    fetchLoanById(parentLoanId)
                } else {
                    _state.value = LoanDetailState.Error("Lỗi khi xóa sách khỏi phiếu")
                }
            } catch (e: Exception) {
                _state.value = LoanDetailState.Error("Lỗi kết nối khi xóa sách")
            }
        }
    }
}