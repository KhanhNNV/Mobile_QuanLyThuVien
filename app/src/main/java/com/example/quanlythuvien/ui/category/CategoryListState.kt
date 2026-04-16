package com.example.quanlythuvien.ui.category

import com.example.quanlythuvien.data.model.response.CategoryResponse

sealed class CategoryListState {
    object Idle : CategoryListState()
    object Loading : CategoryListState()
    data class Success(val categories: List<CategoryResponse>) : CategoryListState()
    data class Error(val message: String) : CategoryListState()
}