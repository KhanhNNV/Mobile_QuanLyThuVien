package com.example.quanlythuvien.ui.reader

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quanlythuvien.data.model.request.ReaderRequest
import com.example.quanlythuvien.data.model.response.LoanDetailResponse
import com.example.quanlythuvien.data.model.response.ReaderResponse
import com.example.quanlythuvien.data.repository.LoanDetailRepository
import com.example.quanlythuvien.data.repository.ReaderRepository
import kotlinx.coroutines.launch

class ReaderDetailViewModel(private val repository: ReaderRepository, private val loanDetailRepo: LoanDetailRepository): ViewModel() {
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

    private val _loanList = MutableLiveData<List<LoanDetailResponse>>()
    val loanList: LiveData<List<LoanDetailResponse>> get() = _loanList
    fun getReaderDetail(readerId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.getReaderById(readerId)
                if (response.isSuccessful && response.body() != null) {
                    _readerData.value = response.body()!!
                } else {
                    _error.value = "Không thể tải chi tiết: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Lỗi kết nối: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    //Hàm update
    fun updateReader(readerId: Long, request: ReaderRequest) {
        viewModelScope.launch {
            //Cờ để biết đang gửi
            _isLoading.value = true
            try {
                val reponse = repository.editReader(readerId, request)
                if (reponse.isSuccessful) {
                    _updateSuccess.value = reponse.body()
                } else {
                    _error.value = "Không thể cập nhật: ${reponse.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Lỗi kết nối: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteReader(readerId: Long) {
        viewModelScope.launch {
            //Cờ để biết đang gửi
            _isLoading.value = true
            try {
                val reponse = repository.deletedReader(readerId)
                if (reponse.isSuccessful) {
                    _deleteSuccess.value = true
                } else {
                    _error.value = "Không thể xóa: ${reponse.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Lỗi kết nối: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchReaderLoans(readerId: Long, status: String, page: Int = 0) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = loanDetailRepo.getReaderLoans(readerId, status, page, 20)
                if (response.isSuccessful) {
                    _loanList.value = response.body()?.content ?: emptyList()
                } else {
                    _error.value = "Lỗi lấy danh sách sách: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Lỗi kết nối: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}