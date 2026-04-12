package com.example.quanlythuvien.ui.dashboard

sealed class AlertState {
    object Idle : AlertState()
    object Loading : AlertState()
    data class Success(val alerts: List<String>) : AlertState()
    data class Error(val message: String) : AlertState()
}