package com.example.quanlythuvien.ui.reader

import com.example.quanlythuvien.data.model.response.ReaderResponse

sealed class ReaderAddState {
    object Idle : ReaderAddState()
    object Loading : ReaderAddState()
    data class Success(val data: ReaderResponse) : ReaderAddState()
    data class Error(val message: String) : ReaderAddState()
}