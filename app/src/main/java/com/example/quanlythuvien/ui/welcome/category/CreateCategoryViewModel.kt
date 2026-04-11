package com.example.quanlythuvien.ui.welcome.category

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quanlythuvien.data.model.request.CategoryRequest
import com.example.quanlythuvien.data.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CreateCategoryViewModel(private val repository: CategoryRepository) : ViewModel() {
    private val _state = MutableStateFlow<CategoryState>(CategoryState.Idle)
    val state: StateFlow<CategoryState> = _state

    fun createCategory(name: String, libraryId: Long) {
        viewModelScope.launch {
            _state.value = CategoryState.Loading
            try {
                val request = CategoryRequest(name, libraryId)
                val response = repository.createCategory(request)
                if (response.isSuccessful && response.body() != null) {
                    // Truyền ID nhận được từ API vào state
                    _state.value = CategoryState.Success(response.body()!!.categoryId)
                } else {
                    _state.value = CategoryState.Error("Lỗi: ${response.code()}")
                }
            } catch (e: Exception) {
                _state.value = CategoryState.Error("Không thể kết nối đến server!")
            }
        }
    }
}