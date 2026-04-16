package com.example.quanlythuvien.data.model.request

import com.google.gson.annotations.SerializedName

data class UpdateUserRequest(

    @SerializedName("username")
    val username: String? = null,

    @SerializedName("fullname")
    val fullname: String? = null,

    @SerializedName("role")
    val role: String? = null,

    @SerializedName("isActive")
    val isActive: Boolean? = null,

    @SerializedName("password")
    val password: String? = null
)

