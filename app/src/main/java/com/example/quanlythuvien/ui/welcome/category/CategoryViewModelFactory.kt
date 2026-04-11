package com.example.quanlythuvien.ui.welcome.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.quanlythuvien.data.repository.CategoryRepository

class CategoryViewModelFactory(private val repository: CategoryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CreateCategoryViewModel(repository) as T
    }
}