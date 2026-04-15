package com.example.quanlythuvien.ui.loan_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quanlythuvien.data.model.request.LoanDetailRequest
import com.example.quanlythuvien.data.repository.LoanDetailRepository
import com.example.quanlythuvien.data.repository.LoanRepository
import com.example.quanlythuvien.ui.borrow_pay.data.BookData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log

class LoanDetailViewModel(
    private val loanRepo: LoanRepository,
    private val detailRepo: LoanDetailRepository
) : ViewModel() {

    private val _state = MutableStateFlow<LoanDetailState>(LoanDetailState.Idle)
    val state: StateFlow<LoanDetailState> = _state

    // Thêm 2 dòng này vào đầu ViewModel
    private val _availableBooks = MutableStateFlow<List<BookData>>(emptyList())
    val availableBooks: StateFlow<List<BookData>> = _availableBooks


    fun fetchAvailableBooks() {
        viewModelScope.launch {
            try {
                // SỬA Ở ĐÂY: Dùng detailRepo thay vì LoanDetailRepository
                val response = detailRepo.getAvailableCopies()

                if (response.isSuccessful && response.body() != null) {
                    val bookDataList = response.body()!!.map { dto ->
                        BookData(
                            copyId = dto.copyId,
                            title = dto.title ?: "Không rõ",
                            author = dto.author ?: "Không rõ",

                            // Lưu ý: Nếu trong BookCopyResponse của bạn không khai báo biến 'category'
                            // thì chỗ này hãy gõ cứng "Không rõ" hoặc dùng dto.condition nhé.
                            categoryName = "Không rõ",

                            barcode = dto.barcode ?: ""
                        )
                    }
                    _availableBooks.value = bookDataList
                    Log.d("DEBUG_SPINNER", "Thành công! Lấy được ${bookDataList.size} cuốn sách")
                } else {
                    // THÊM DÒNG LOG NÀY:
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

    // 3. Cập nhật trạng thái một cuốn sách (Sửa sách)
    fun updateBookInLoan(loanId: Long, copyId: Long, request: LoanDetailRequest) {
        viewModelScope.launch {
            try {
                val response = detailRepo.updateLoanDetail(loanId, copyId, request)
                if (response.isSuccessful) {
                    _state.value = LoanDetailState.UpdateBookSuccess
                    // Sau khi sửa xong, tự động gọi lại fetch để refresh UI
                    fetchLoanById(loanId)
                }
            } catch (e: Exception) {
                _state.value = LoanDetailState.Error("Không thể cập nhật thông tin sách")
            }
        }
    }

    // 4. Xóa một cuốn sách khỏi phiếu mượn
    fun deleteBookFromLoan(loanId: Long, copyId: Long) {
        viewModelScope.launch {
            try {
                val response = detailRepo.deleteLoanDetail(loanId, copyId)
                if (response.isSuccessful) {
                    _state.value = LoanDetailState.UpdateBookSuccess
                    fetchLoanById(loanId)
                }
            } catch (e: Exception) {
                _state.value = LoanDetailState.Error("Lỗi khi xóa sách khỏi phiếu")
            }
        }
    }
}