package com.example.quanlythuvien.ui.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quanlythuvien.data.model.request.ReaderRequest
import com.example.quanlythuvien.data.repository.ReaderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReaderAddViewModel(private val repository: ReaderRepository) : ViewModel() {

    private val _addReaderState = MutableStateFlow<ReaderAddState>(ReaderAddState.Idle)
    val addReaderState: StateFlow<ReaderAddState> = _addReaderState

    fun addReader(request: ReaderRequest) {
        viewModelScope.launch {
            _addReaderState.value = ReaderAddState.Loading
            try {
                val response = repository.createReader(request)
                if (response.isSuccessful && response.body() != null) {
                    _addReaderState.value = ReaderAddState.Success(response.body()!!)
                } else {
                    _addReaderState.value = ReaderAddState.Error("Lỗi: ${response.code()}")
                }
            } catch (e: Exception) {
                _addReaderState.value = ReaderAddState.Error("Mất kết nối: ${e.message}")
            }
        }
    }
}