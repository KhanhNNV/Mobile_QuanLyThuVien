package com.example.quanlythuvien.ui.welcome.book

import com.example.quanlythuvien.data.model.response.InitialBookResponse

sealed class BookState {
    object Idle : BookState()
    object Loading : BookState()
    data class Success(val data: InitialBookResponse) : BookState()
    data class Error(val message: String) : BookState()
}