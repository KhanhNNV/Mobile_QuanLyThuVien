package com.example.quanlythuvien.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quanlythuvien.data.model.request.CategoryRequest
import com.example.quanlythuvien.data.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CategoryAddViewModel(private val repository: CategoryRepository) : ViewModel() {

    private val _addCategoryState = MutableStateFlow<CategoryAddState>(CategoryAddState.Idle)
    val addCategoryState: StateFlow<CategoryAddState> = _addCategoryState

    fun addCategory(request: CategoryRequest) {
        viewModelScope.launch {
            _addCategoryState.value = CategoryAddState.Loading
            try {
                val response = repository.createCategory(request)
                if (response.isSuccessful && response.body() != null) {
                    _addCategoryState.value = CategoryAddState.Success(response.body()!!)
                } else {
                    _addCategoryState.value = CategoryAddState.Error("Lỗi: ${response.code()}")
                }
            } catch (e: Exception) {
                _addCategoryState.value = CategoryAddState.Error("Mất kết nối: ${e.message}")
            }
        }
    }
}