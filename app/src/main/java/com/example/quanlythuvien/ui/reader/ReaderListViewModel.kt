package com.example.quanlythuvien.ui.reader

import android.app.Application
import androidx.lifecycle.*
import com.example.quanlythuvien.core.api.RetrofitClient
import com.example.quanlythuvien.data.model.response.ReaderResponse
import com.example.quanlythuvien.data.remote.ReaderApiService
import com.example.quanlythuvien.data.repository.ReaderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.debounce

class ReaderListViewModel(application: Application) : AndroidViewModel(application) {
    // Khởi tạo API Service và Repository để giao tiếp với Backend
    private val apiService = RetrofitClient.getInstance(application).create(ReaderApiService::class.java)
    private val readerRepository = ReaderRepository(apiService)


    private val _readers = MutableLiveData<List<ReaderResponse>>(emptyList())
    val allReader: LiveData<List<ReaderResponse>> get() = _readers

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error


    //Giữ các từ khóa tìm kiếm
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // CÁC BIẾN QUẢN LÝ TRẠNG THÁI PHÂN TRANG
    private var currentPage = 0
    private var isLastPage = false
    private var isLoading = false
    private var isSearchMode = false

    init {
        loadInitialReaders() // Tự động tải trang đầu tiên khi ViewModel khởi tạo

        setupSearchListener()
    }

    /**
     * Hàm tải lại từ đầu (dùng cho lần đầu mở máy hoặc khi người dùng Refresh)
     */
    fun loadInitialReaders() {
        isSearchMode = false
        currentPage = 0
        isLastPage = false
        _readers.value = emptyList()
        fetchReaders()
    }

    /**
     * Hàm yêu cầu tải thêm trang tiếp theo khi người dùng cuộn xuống cuối
     */
    fun loadMoreReaders() {
        if (isLoading || isLastPage || isSearchMode) return
        currentPage++
        fetchReaders()
    }

    fun searchReaders(query: String) {
        val keyword = query.trim()
        if (keyword.isEmpty()) {
            loadInitialReaders()
            return
        }

        isSearchMode = true
        isLoading = true
        viewModelScope.launch {
            try {
                val response = readerRepository.searchReaders(keyword)
                if (response.isSuccessful) {
                    _readers.value = response.body().orEmpty()
                    isLastPage = true
                } else {
                    _error.value = "Không thể tìm kiếm: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Lỗi tìm kiếm: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Hàm lấy dữ liệu từ API và cập nhật vào LiveData
     */
    private fun fetchReaders() {
        isLoading = true //Bật cờ tránh gọi nhiều lần API
        viewModelScope.launch {
            try {
                // Gọi Repository lấy dữ liệu theo Page và Size (10)
                val response = readerRepository.getReadersPaginated(currentPage, 10)
                if (response.isSuccessful) {
                    response.body()?.let { pageData ->
                        // Cộng dồn danh sách cũ với danh sách mới
                        val currentList = _readers.value.orEmpty().toMutableList()
                        currentList.addAll(pageData.content)
                        _readers.value = currentList
                        //Cập nhật lại biến cờ trang cuối
                        isLastPage = pageData.last
                    }
                } else {
                    _error.value = "Không thể lấy dữ liệu: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Lỗi kết nối: ${e.message}"
            } finally {
                isLoading = false // Hoàn tất quá trình tải, hạ cờ xuống để cho phép gọi lần sau
            }
        }
    }

    private fun setupSearchListener() {
        viewModelScope.launch {
            _searchQuery
                .debounce(500)                     // Đợi 0.5s sau lần gõ cuối
                .distinctUntilChanged()            // Không gọi lại nếu từ khóa không đổi
                .collect { query ->
                    performSearch(query.trim())
                }
        }
    }

    /**
     * Hàm này được Fragment gọi mỗi khi người dùng gõ (để cập nhật Flow)
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }


    /**
     * Thực sự gọi API tìm kiếm
     */
    private suspend fun performSearch(query: String) {
        if (query.isEmpty()) {
            // Xóa trắng -> quay lại danh sách phân trang
            loadInitialReaders()
            return
        }

        isSearchMode = true
        isLoading = true
        try {
            val response = readerRepository.searchReaders(query)
            if (response.isSuccessful) {
                _readers.value = response.body().orEmpty()
                isLastPage = true   // Tìm kiếm trả về toàn bộ, không phân trang
            } else {
                _error.value = "Không thể tìm kiếm: ${response.code()}"
            }
        } catch (e: Exception) {
            _error.value = "Lỗi tìm kiếm: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    // Các hàm helper để Fragment kiểm tra trạng thái mà không can thiệp trực tiếp vào biến private
    fun isLoading() = isLoading
    fun isLastPage() = isLastPage
}