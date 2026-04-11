package com.example.quanlythuvien.ui.welcome.book

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quanlythuvien.data.model.request.InitialBookRequest
import com.example.quanlythuvien.data.repository.BookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CreateBookViewModel(private val repository: BookRepository) : ViewModel() {
    private val _state = MutableStateFlow<BookState>(BookState.Idle)
    val state: StateFlow<BookState> = _state

    fun createInitialBook(request: InitialBookRequest) {
        viewModelScope.launch {
            _state.value = BookState.Loading
            try {
                val response = repository.createInitialBook(request)
                if (response.isSuccessful && response.body() != null) {
                    _state.value = BookState.Success(response.body()!!)
                } else {
                    _state.value = BookState.Error("Lỗi: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                _state.value = BookState.Error("Không thể kết nối đến server!")
            }
        }
    }
}