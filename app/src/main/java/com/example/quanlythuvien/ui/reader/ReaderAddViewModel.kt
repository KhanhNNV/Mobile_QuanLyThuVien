package com.example.quanlythuvien.ui.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quanlythuvien.core.network.ApiErrorParser
import com.example.quanlythuvien.data.model.request.ReaderRequest
import com.example.quanlythuvien.data.model.response.ReaderResponse
import com.example.quanlythuvien.data.repository.ReaderRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class ReaderAddViewModel(private val repository: ReaderRepository) : ViewModel() {

    private val _addReaderState = MutableStateFlow<ReaderAddState>(ReaderAddState.Idle)
    val addReaderState: StateFlow<ReaderAddState> = _addReaderState
    private val _readerDetail = MutableStateFlow<ReaderResponse?>(null)
    val readerDetail: StateFlow<ReaderResponse?> = _readerDetail

    private val _detailLoadError = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val detailLoadError: SharedFlow<String> = _detailLoadError.asSharedFlow()

    fun addReader(request: ReaderRequest) {
        viewModelScope.launch {
            _addReaderState.value = ReaderAddState.Loading
            try {
                val response = repository.createReader(request)
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    _addReaderState.value = ReaderAddState.Success(body)
                } else {
                    _addReaderState.value = ReaderAddState.Error(
                        ApiErrorParser.parseErrorMessage(response, "Không thể thêm độc giả.")
                    )
                }
            } catch (e: Exception) {
                _addReaderState.value = ReaderAddState.Error("Mất kết nối: ${e.message ?: "không xác định"}")
            }
        }
    }

    fun updateReader(readerId: Long, request: ReaderRequest) {
        viewModelScope.launch {
            _addReaderState.value = ReaderAddState.Loading
            try {
                val response = repository.editReader(readerId, request)
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    _addReaderState.value = ReaderAddState.Success(body)
                } else {
                    _addReaderState.value = ReaderAddState.Error(
                        ApiErrorParser.parseErrorMessage(response, "Không thể cập nhật độc giả.")
                    )
                }
            } catch (e: Exception) {
                _addReaderState.value = ReaderAddState.Error("Mất kết nối: ${e.message ?: "không xác định"}")
            }
        }
    }

    fun getReaderDetail(readerId: Long) {
        viewModelScope.launch {
            try {
                val response = repository.getReaderById(readerId)
                if (response.isSuccessful && response.body() != null) {
                    _readerDetail.value = response.body()
                } else {
                    _detailLoadError.tryEmit(ApiErrorParser.parseErrorMessage(response, "Không thể tải chi tiết độc giả."))
                }
            } catch (e: Exception) {
                _detailLoadError.tryEmit("Mất kết nối: ${e.message ?: "không xác định"}")
            }
        }
    }
}