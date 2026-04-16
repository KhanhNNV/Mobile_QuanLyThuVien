package com.example.quanlythuvien.data.model.response

import com.google.gson.annotations.SerializedName

data class UserResponse(
    @SerializedName("userId")
    val userId: Long,

    @SerializedName("username")
    val username: String,

    @SerializedName("fullname")
    val fullname: String,

    @SerializedName("role")
    val role: String, // ADMIN, STAFF

    @SerializedName("isActive")
    val isActive: Boolean,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("updateAt")
    val updateAt: String?
)