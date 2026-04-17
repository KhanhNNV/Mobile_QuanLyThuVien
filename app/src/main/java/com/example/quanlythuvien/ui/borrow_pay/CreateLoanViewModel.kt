package com.example.quanlythuvien.ui.borrow_pay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quanlythuvien.core.network.ApiErrorParser
import com.example.quanlythuvien.data.model.request.CreateLoanWithDetailsRequest
import com.example.quanlythuvien.data.repository.BookCopyRepository
import com.example.quanlythuvien.data.repository.LoanRepository
import com.example.quanlythuvien.data.repository.ReaderRepository
import com.example.quanlythuvien.ui.borrow_pay.data.BookDropDownItem
import com.example.quanlythuvien.ui.borrow_pay.data.CreateLoanUiState
import com.example.quanlythuvien.ui.borrow_pay.data.ReaderDropDownItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateLoanViewModel(
    private val loanRepository: LoanRepository,
    private val readerRepository: ReaderRepository,
    private val bookCopyRepository: BookCopyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateLoanUiState())
    val uiState: StateFlow<CreateLoanUiState> = _uiState.asStateFlow()

    init {
        fetchInitialData()
    }

    private fun fetchInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // Nhận về đối tượng Response từ Retrofit
                val readersResponse = readerRepository.getAllReaders()
                val booksResponse = bookCopyRepository.getAvailableCopies()

                // map body của api trả về sang UI
                val readerItems = if (readersResponse.isSuccessful) {
                    readersResponse.body()?.map { ReaderDropDownItem(it) } ?: emptyList()
                } else {
                    emptyList()
                }

                val bookItems = if (booksResponse.isSuccessful) {
                    booksResponse.body()?.map { BookDropDownItem(it) } ?: emptyList()
                } else {
                    emptyList()
                }

                // 3. Cập nhật UI State
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        readers = readerItems,
                        availableBooks = bookItems
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun selectReader(reader: ReaderDropDownItem) {
        _uiState.update { it.copy(selectedReader = reader) }
    }
    fun addBookToSelection(book: BookDropDownItem) {
        _uiState.update { state ->
            val currentList = state.selectedBooksForLoan.toMutableList()

            // Kiểm tra xem sách này đã được thêm vào danh sách mượn chưa
            val isAlreadyAdded = currentList.any { it.bookCopy.copyId == book.bookCopy.copyId }
            if (!isAlreadyAdded) {
                currentList.add(book)
            }

            state.copy(selectedBooksForLoan = currentList)
        }
    }

    fun removeBookFromSelection(bookIdToRemove: Long) {
        _uiState.update { state ->
            val currentList = state.selectedBooksForLoan.toMutableList()
            currentList.removeAll { it.bookCopy.copyId == bookIdToRemove }
            state.copy(selectedBooksForLoan = currentList)
        }
    }

    fun clearErrorAndViolations() {
        _uiState.update { it.copy(error = null, violationMessages = emptyList()) }
    }

    fun createLoan() {
        val currentState = _uiState.value

        if (currentState.selectedReader == null) {
            _uiState.update { it.copy(error = "Vui lòng chọn độc giả!") }
            return
        }
        if (currentState.selectedBooksForLoan.isEmpty()) {
            _uiState.update { it.copy(error = "Vui lòng chọn ít nhất 1 cuốn sách!") }
            return
        }

        // Chuẩn bị Request
        val readerId = currentState.selectedReader.reader.readerId
        val copyIds = currentState.selectedBooksForLoan.map { it.bookCopy.copyId }
        val request = CreateLoanWithDetailsRequest(readerId, copyIds)

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, violationMessages = emptyList()) }
            try {
                val response = loanRepository.createLoanWithDetails(request)

                if (response.isSuccessful) {
                    _uiState.update { it.copy(isLoading = false, isCreateSuccess = true) }
                } else {
                    val message = ApiErrorParser.parseRawError(
                        response.errorBody()?.string(),
                        "Lỗi không xác định từ máy chủ"
                    )
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            error = message,
                            violationMessages = if (message.contains("\n")) message.split("\n").filter { it.isNotBlank() } else emptyList()
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Mất kết nối: ${e.message ?: "không xác định"}") }
            }
        }
    }
}
