package com.example.quanlythuvien.ui.reader

import android.app.Application
import android.net.Network
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.quanlythuvien.core.api.RetrofitClient
import com.example.quanlythuvien.data.remote.ReaderApiService
import com.example.quanlythuvien.data.model.response.ReaderResponse
import com.example.quanlythuvien.data.repository.ReaderRepository
import kotlinx.coroutines.launch

class ReaderListViewModel(application: Application) : AndroidViewModel(application) {
    private val apiService = RetrofitClient.getInstance(application).create(ReaderApiService::class.java)
    private val readerRepository = ReaderRepository(apiService)
    private val _readers = MutableLiveData<List<ReaderResponse>>(emptyList())
    val allReader: LiveData<List<ReaderResponse>> get() = _readers

    var currentPage = 0
    var isLastPage = false
    var isLoading = false

    init {
        fetchReaders()
    }

    fun fetchReaders() {
        if (isLoading || isLastPage) return
        isLoading = true
        viewModelScope.launch {
            try {
                val response = readerRepository.getReadersPaginated(currentPage, 10)
                if (response.isSuccessful) {
                    response.body()?.let {
                        val updatedList = _readers.value.orEmpty() + it.content
                        _readers.value = updatedList
                        isLastPage = it.last
                        if (!isLastPage) currentPage++
                    }
                } else {
                    // THÊM: Báo lỗi nếu API trả về 400, 401, 403, 500...
                    Log.e("API_ERROR", "Lỗi Server: ${response.code()} - ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                // THÊM: Báo lỗi nếu rớt mạng hoặc sai cấu trúc JSON
                Log.e("API_ERROR", "Lỗi Cú pháp/Mạng: ${e.message}", e)
            } finally {
                isLoading = false
            }
        }
    }
}