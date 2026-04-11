package com.example.quanlythuvien.ui.welcome.category

sealed class CategoryState {
    object Idle : CategoryState()
    object Loading : CategoryState()
    data class Success(val categoryId: Long) : CategoryState()
    data class Error(val message: String) : CategoryState()
}