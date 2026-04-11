package com.example.quanlythuvien.data.model.request

data class RegisterRequest(
    val username: String,
    val password: String,
    val fullName: String,
    val libraryName: String,
    val address: String,
    val hasStudentDiscount: Boolean
)
