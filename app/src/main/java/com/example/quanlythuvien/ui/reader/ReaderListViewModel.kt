package com.example.quanlythuvien.ui.reader

import android.app.Application
import androidx.lifecycle.*
import com.example.quanlythuvien.core.api.RetrofitClient
import com.example.quanlythuvien.data.model.response.ReaderResponse
import com.example.quanlythuvien.data.remote.ReaderApiService
import com.example.quanlythuvien.data.repository.ReaderRepository
import kotlinx.coroutines.launch

class ReaderListViewModel(application: Application) : AndroidViewModel(application) {
    // Khởi tạo API Service và Repository để giao tiếp với Backend
    private val apiService = RetrofitClient.getInstance(application).create(ReaderApiService::class.java)
    private val readerRepository = ReaderRepository(apiService)


    private val _readers = MutableLiveData<List<ReaderResponse>>(emptyList())
    val allReader: LiveData<List<ReaderResponse>> get() = _readers

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    // CÁC BIẾN QUẢN LÝ TRẠNG THÁI PHÂN TRANG
    private var currentPage = 0
    private var isLastPage = false
    private var isLoading = false

    init {
        loadInitialReaders() // Tự động tải trang đầu tiên khi ViewModel khởi tạo
    }

    /**
     * Hàm tải lại từ đầu (dùng cho lần đầu mở máy hoặc khi người dùng Refresh)
     */
    fun loadInitialReaders() {
        currentPage = 0
        isLastPage = false
        _readers.value = emptyList()
        fetchReaders()
    }

    /**
     * Hàm yêu cầu tải thêm trang tiếp theo khi người dùng cuộn xuống cuối
     */
    fun loadMoreReaders() {
        if (isLoading || isLastPage) return
        currentPage++
        fetchReaders()
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
    // Các hàm helper để Fragment kiểm tra trạng thái mà không can thiệp trực tiếp vào biến private
    fun isLoading() = isLoading
    fun isLastPage() = isLastPage
}