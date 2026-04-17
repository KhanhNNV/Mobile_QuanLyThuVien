package com.example.quanlythuvien.data.model.request

data class UserRequest(
    val username: String,
    val password: String,
    val fullname: String,
    val role: String,
    val isActive: Boolean? = true
)