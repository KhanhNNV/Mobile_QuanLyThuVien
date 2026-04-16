package com.example.quanlythuvien.ui.category

sealed class CategoryEditState {
    object Idle : CategoryEditState()
    object Loading : CategoryEditState()
    object UpdateSuccess : CategoryEditState()
    object DeleteSuccess : CategoryEditState()
    data class Error(val message: String) : CategoryEditState()
}