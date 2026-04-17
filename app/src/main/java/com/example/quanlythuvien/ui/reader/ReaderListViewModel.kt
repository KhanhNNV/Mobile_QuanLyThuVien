package com.example.quanlythuvien.ui.reader

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.quanlythuvien.core.api.RetrofitClient
import com.example.quanlythuvien.core.network.ApiErrorParser
import com.example.quanlythuvien.data.model.response.ReaderResponse
import com.example.quanlythuvien.data.remote.ReaderApiService
import com.example.quanlythuvien.data.repository.ReaderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class ReaderListViewModel(application: Application) : AndroidViewModel(application) {
    private val apiService = RetrofitClient.getInstance(application).create(ReaderApiService::class.java)
    private val readerRepository = ReaderRepository(apiService)

    private val _readers = MutableLiveData<List<ReaderResponse>>(emptyList())
    val allReader: LiveData<List<ReaderResponse>> get() = _readers

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val _searchQuery = MutableStateFlow("")

    private var currentPage = 0
    private var isLastPage = false
    private var isLoading = false
    private var isSearchMode = false

    init {
        loadInitialReaders()
        setupSearchListener()
    }

    fun loadInitialReaders() {
        isSearchMode = false
        currentPage = 0
        isLastPage = false
        _readers.value = emptyList()
        fetchReaders()
    }

    fun loadMoreReaders() {
        if (isLoading || isLastPage || isSearchMode) return
        currentPage++
        fetchReaders()
    }

    private fun fetchReaders() {
        isLoading = true
        viewModelScope.launch {
            try {
                val response = readerRepository.getReadersPaginated(currentPage, 10)
                if (response.isSuccessful) {
                    response.body()?.let { pageData ->
                        val currentList = _readers.value.orEmpty().toMutableList()
                        currentList.addAll(pageData.content)
                        _readers.value = currentList
                        isLastPage = pageData.last
                    }
                } else {
                    _error.value = ApiErrorParser.parseErrorMessage(response, "Không thể lấy danh sách độc giả.")
                }
            } catch (e: Exception) {
                _error.value = "Lỗi kết nối: ${e.message ?: "không xác định"}"
            } finally {
                isLoading = false
            }
        }
    }

    private fun setupSearchListener() {
        viewModelScope.launch {
            _searchQuery
                .debounce(500)
                .distinctUntilChanged()
                .collect { query ->
                    performSearch(query.trim())
                }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    private suspend fun performSearch(query: String) {
        if (query.isEmpty()) {
            loadInitialReaders()
            return
        }

        isSearchMode = true
        isLoading = true
        try {
            val response = readerRepository.searchReaders(query)
            if (response.isSuccessful) {
                _readers.value = response.body().orEmpty()
                isLastPage = true
            } else {
                _error.value = ApiErrorParser.parseErrorMessage(response, "Không thể tìm kiếm độc giả.")
            }
        } catch (e: Exception) {
            _error.value = "Lỗi tìm kiếm: ${e.message ?: "không xác định"}"
        } finally {
            isLoading = false
        }
    }

    fun isLoading() = isLoading
    fun isLastPage() = isLastPage
}
