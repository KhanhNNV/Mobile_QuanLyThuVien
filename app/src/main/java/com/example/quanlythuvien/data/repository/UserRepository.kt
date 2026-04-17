package com.example.quanlythuvien.data.repository

import com.example.quanlythuvien.data.model.request.UpdateUserRequest
import com.example.quanlythuvien.data.model.request.UserRequest
import com.example.quanlythuvien.data.remote.UserApiService

class UserRepository(private val apiService: UserApiService) {

    suspend fun getAllUsers() = apiService.getAllUsers()


    suspend fun getUserById(id: Long) = apiService.getUserById(id)

    suspend fun updateUser(id: Long, request: UpdateUserRequest) =
        apiService.updateUser(id, request)

    suspend fun deleteUser(id: Long) = apiService.deleteUser(id)

    suspend fun createUser(request: UserRequest) = apiService.createUser(request)

}