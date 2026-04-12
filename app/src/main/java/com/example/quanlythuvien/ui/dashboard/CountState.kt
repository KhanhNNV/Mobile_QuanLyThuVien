package com.example.quanlythuvien.ui.dashboard

sealed class CountState {
    object Idle : CountState()
    object Loading : CountState()
    data class Success(val count: Long) : CountState()
    data class Error(val message: String) : CountState()
}