package com.example.quanlythuvien.data.model.response

/**
 * Class dùng chung để nhận các dữ liệu phân trang từ server
 */
data class PageResponse<T>(
    val content : List<T>,
    val totalPages : Int,
    val number : Int,
    val size : Int,
    val first : Boolean,
    val last : Boolean,
)