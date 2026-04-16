package com.example.quanlythuvien.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quanlythuvien.data.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CategoryListViewModel(private val repository: CategoryRepository) : ViewModel() {

    private val _categoryListState = MutableStateFlow<CategoryListState>(CategoryListState.Idle)
    val categoryListState: StateFlow<CategoryListState> = _categoryListState

    fun fetchCategories() {
        viewModelScope.launch {
            _categoryListState.value = CategoryListState.Loading
            try {
                val response = repository.getCategories()
                if (response.isSuccessful && response.body() != null) {
                    _categoryListState.value = CategoryListState.Success(response.body()!!)
                } else {
                    _categoryListState.value = CategoryListState.Error("Lỗi: ${response.code()}")
                }
            } catch (e: Exception) {
                _categoryListState.value = CategoryListState.Error("Mất kết nối: ${e.message}")
            }
        }
    }
}