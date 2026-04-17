package com.example.quanlythuvien.ui.reader

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quanlythuvien.core.network.ApiErrorParser
import com.example.quanlythuvien.data.model.request.ReaderRequest
import com.example.quanlythuvien.data.model.response.ReaderResponse
import com.example.quanlythuvien.data.repository.ReaderRepository
import kotlinx.coroutines.launch

class ReaderDetailViewModel(private val repository: ReaderRepository) : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val _updateSuccess = MutableLiveData<ReaderResponse>()
    val updateSuccess: LiveData<ReaderResponse> get() = _updateSuccess

    private val _deleteSuccess = MutableLiveData<Boolean>()
    val deleteSuccess: LiveData<Boolean> get() = _deleteSuccess

    private val _readerData = MutableLiveData<ReaderResponse>()
    val readerData: LiveData<ReaderResponse> get() = _readerData

    fun getReaderDetail(readerId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.getReaderById(readerId)
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    _readerData.value = body
                } else {
                    _error.value = ApiErrorParser.parseErrorMessage(response, "Không thể tải chi tiết độc giả.")
                }
            } catch (e: Exception) {
                _error.value = "Lỗi kết nối: ${e.message ?: "không xác định"}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateReader(readerId: Long, request: ReaderRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.editReader(readerId, request)
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    _updateSuccess.value = body
                } else {
                    _error.value = ApiErrorParser.parseErrorMessage(response, "Không thể cập nhật độc giả.")
                }
            } catch (e: Exception) {
                _error.value = "Lỗi kết nối: ${e.message ?: "không xác định"}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteReader(readerId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.deletedReader(readerId)
                if (response.isSuccessful) {
                    _deleteSuccess.value = true
                } else {
                    _error.value = ApiErrorParser.parseErrorMessage(response, "Không thể xóa độc giả.")
                }
            } catch (e: Exception) {
                _error.value = "Lỗi kết nối: ${e.message ?: "không xác định"}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}