package com.example.quanlythuvien.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quanlythuvien.data.repository.BookRepository
import com.example.quanlythuvien.data.repository.LoanRepository
import com.example.quanlythuvien.data.repository.ReaderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val bookRepository: BookRepository,
    private val loanRepository: LoanRepository,
    private val readerRepository: ReaderRepository
) : ViewModel() {

    // State Tổng sách
    private val _bookCountState = MutableStateFlow<CountState>(CountState.Idle)
    val bookCountState: StateFlow<CountState> = _bookCountState

    // State Đang mượn
    private val _borrowingLoanState = MutableStateFlow<CountState>(CountState.Idle)
    val borrowingLoanState: StateFlow<CountState> = _borrowingLoanState

    // State Trễ hạn
    private val _overdueLoanState = MutableStateFlow<CountState>(CountState.Idle)
    val overdueLoanState: StateFlow<CountState> = _overdueLoanState

    // State Độc giả
    private val _readerCountState = MutableStateFlow<CountState>(CountState.Idle)
    val readerCountState: StateFlow<CountState> = _readerCountState


    fun loadTotalBooks(libraryId: Long) {
        viewModelScope.launch {
            _bookCountState.value = CountState.Loading
            try {
                val response = bookRepository.countBooksByLibrary(libraryId)
                if (response.isSuccessful && response.body() != null) {
                    _bookCountState.value = CountState.Success(response.body()!!)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Lỗi mã: ${response.code()}"
                    _bookCountState.value = CountState.Error("Lỗi server: $errorMsg")
                }
            } catch (e: Exception) {
                _bookCountState.value = CountState.Error(e.message ?: "Mất kết nối mạng")
            }
        }
    }

    fun loadBorrowingLoans(libraryId: Long) {
        viewModelScope.launch {
            _borrowingLoanState.value = CountState.Loading
            try {
                val response = loanRepository.countBorrowingLoans(libraryId)
                if (response.isSuccessful && response.body() != null) {
                    _borrowingLoanState.value = CountState.Success(response.body()!!)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Lỗi mã: ${response.code()}"
                    _borrowingLoanState.value = CountState.Error("Lỗi server: $errorMsg")
                }
            } catch (e: Exception) {
                _borrowingLoanState.value = CountState.Error(e.message ?: "Mất kết nối mạng")
            }
        }
    }

    fun loadOverdueLoans(libraryId: Long) {
        viewModelScope.launch {
            _overdueLoanState.value = CountState.Loading
            try {
                val response = loanRepository.countOverdueLoans(libraryId)
                if (response.isSuccessful && response.body() != null) {
                    _overdueLoanState.value = CountState.Success(response.body()!!)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Lỗi mã: ${response.code()}"
                    _overdueLoanState.value = CountState.Error("Lỗi server: $errorMsg")
                }
            } catch (e: Exception) {
                _overdueLoanState.value = CountState.Error(e.message ?: "Mất kết nối mạng")
            }
        }
    }

    fun loadTotalReaders(libraryId: Long) {
        viewModelScope.launch {
            _readerCountState.value = CountState.Loading
            try {
                val response = readerRepository.countReaders(libraryId)
                if (response.isSuccessful && response.body() != null) {
                    _readerCountState.value = CountState.Success(response.body()!!)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Lỗi mã: ${response.code()}"
                    _readerCountState.value = CountState.Error("Lỗi server: $errorMsg")
                }
            } catch (e: Exception) {
                _readerCountState.value = CountState.Error(e.message ?: "Mất kết nối mạng")
            }
        }
    }
}