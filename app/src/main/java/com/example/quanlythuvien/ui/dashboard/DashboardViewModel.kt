package com.example.quanlythuvien.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quanlythuvien.data.repository.BookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(private val repository: BookRepository) : ViewModel() {

    private val _bookCountState = MutableStateFlow<DashboardBookCountState>(DashboardBookCountState.Idle)
    val bookCountState: StateFlow<DashboardBookCountState> = _bookCountState

    fun loadTotalBooks(libraryId: Long) {
        viewModelScope.launch {
            // Đẩy trạng thái sang Loading trước khi gọi API
            _bookCountState.value = DashboardBookCountState.Loading

            try {
                val response = repository.countBooksByLibrary(libraryId)

                if (response.isSuccessful && response.body() != null) {
                    _bookCountState.value = DashboardBookCountState.Success(response.body()!!)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Lỗi mã: ${response.code()}"
                    _bookCountState.value = DashboardBookCountState.Error("Lỗi server: $errorMsg")
                }
            } catch (e: Exception) {
                // Lỗi crash mạng hoặc server sập
                _bookCountState.value = DashboardBookCountState.Error(e.message ?: "Mất kết nối mạng")
            }
        }
    }
}