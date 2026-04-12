package com.example.quanlythuvien.ui.dashboard

sealed class DashboardBookCountState {
    object Idle : DashboardBookCountState()
    object Loading : DashboardBookCountState()
    data class Success(val totalBooks: Long) : DashboardBookCountState() // Nhận giá trị kiểu Long từ API
    data class Error(val message: String) : DashboardBookCountState()
}