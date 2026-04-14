package com.example.quanlythuvien.ui.books

import com.example.quanlythuvien.data.model.response.BookResponse

sealed class AddBookState {
    object Idle : AddBookState()
    object Loading : AddBookState()
    data class Success(val data: BookResponse) : AddBookState() // Chấp nhận BookResponse của tụi mình
    data class Error(val message: String) : AddBookState()
}