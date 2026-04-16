package com.example.quanlythuvien.ui.category

import com.example.quanlythuvien.data.model.response.CategoryResponse

sealed class CategoryAddState {
    object Idle : CategoryAddState()
    object Loading : CategoryAddState()
    data class Success(val data: CategoryResponse) : CategoryAddState()
    data class Error(val message: String) : CategoryAddState()
}