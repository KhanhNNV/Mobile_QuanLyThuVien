package com.example.quanlythuvien.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedFilterLoanViewModel : ViewModel() {
    // Lưu trữ loại filter, ban đầu là null
    val filterType = MutableLiveData<String?>()

    fun setFilter(type: String) {
        filterType.value = type
    }

    fun clearFilter() {
        filterType.value = null
    }
}