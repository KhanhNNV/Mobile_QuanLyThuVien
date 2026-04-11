package com.example.quanlythuvien.ui.welcome.book

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.quanlythuvien.data.repository.BookRepository

class CreateBookViewModelFactory(private val repository: BookRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CreateBookViewModel(repository) as T
    }
}