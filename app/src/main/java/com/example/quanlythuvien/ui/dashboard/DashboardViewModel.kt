package com.example.quanlythuvien.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quanlythuvien.data.repository.BookRepository
import com.example.quanlythuvien.data.repository.LibraryRepository
import com.example.quanlythuvien.data.repository.LoanDetailRepository
import com.example.quanlythuvien.data.repository.LoanRepository
import com.example.quanlythuvien.data.repository.ReaderRepository
import com.example.quanlythuvien.utils.LibraryConfigManager
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val bookRepository: BookRepository,
    private val loanRepository: LoanRepository,
    private val readerRepository: ReaderRepository,
    private val loanDetailRepository: LoanDetailRepository,
    private val libraryRepository: LibraryRepository,
    private val configManager: LibraryConfigManager
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

    // State Cảnh báo
    private val _alertState = MutableStateFlow<AlertState>(AlertState.Idle)
    val alertState: StateFlow<AlertState> = _alertState


    fun loadTotalBooks() {
        viewModelScope.launch {
            _bookCountState.value = CountState.Loading
            try {
                val response = bookRepository.countBooksByLibrary()
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

    fun loadBorrowingLoans() {
        viewModelScope.launch {
            _borrowingLoanState.value = CountState.Loading
            try {
                val response = loanRepository.countBorrowingLoans()
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

    fun loadOverdueLoans() {
        viewModelScope.launch {
            _overdueLoanState.value = CountState.Loading
            try {
                val response = loanRepository.countOverdueLoans()
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

    fun loadTotalReaders() {
        viewModelScope.launch {
            _readerCountState.value = CountState.Loading
            try {
                val response = readerRepository.countReaders()
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
    fun loadAlerts() {
        viewModelScope.launch {
            _alertState.value = AlertState.Loading
            try {
                // Gọi đồng thời 2 API
                val bookAlertsDeferred = async { bookRepository.getLowCopyAlerts() }
                val loanAlertsDeferred = async { loanDetailRepository.getDueTodayAlerts() }

                // Đợi 2 API trả về kết quả
                val bookResponse = bookAlertsDeferred.await()
                val loanResponse = loanAlertsDeferred.await()

                val allAlerts = mutableListOf<String>()

                if (bookResponse.isSuccessful) {
                    bookResponse.body()?.let { allAlerts.addAll(it) }
                }

                if (loanResponse.isSuccessful) {
                    loanResponse.body()?.let { allAlerts.addAll(it) }
                }

                if (allAlerts.isEmpty()) {
                    allAlerts.add("Không có cảnh báo nào trong hôm nay.")
                }

                _alertState.value = AlertState.Success(allAlerts)

            } catch (e: Exception) {
                _alertState.value = AlertState.Error("Không thể tải cảnh báo: Mất kết nối")
            }
        }
    }

    fun loadLibraryConfig() {
        viewModelScope.launch {
            try {
                val response = libraryRepository.getLibraryConfig()
                if (response.isSuccessful && response.body() != null) {
                    val hasDiscount = response.body()!!.hasStudentDiscount
                    // Lưu thẳng vào SharedPreferences
                    configManager.saveHasStudentDiscount(hasDiscount)
                }
            } catch (e: Exception) {
            }
        }
    }
}