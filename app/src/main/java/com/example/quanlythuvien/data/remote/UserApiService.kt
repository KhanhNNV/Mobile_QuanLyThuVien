package com.example.quanlythuvien.data.remote

import com.example.quanlythuvien.data.model.request.UpdateProfileRequest
import com.example.quanlythuvien.data.model.request.UpdateUserRequest
import com.example.quanlythuvien.data.model.request.UserRequest
import com.example.quanlythuvien.data.model.response.UserResponse
import retrofit2.Response
import retrofit2.http.*

interface UserApiService {

    @GET("api/users")
    suspend fun getAllUsers(): Response<List<UserResponse>>

    @GET("api/users/active")
    suspend fun getActiveUsers(): Response<List<UserResponse>>

    @GET("api/users/{id}")
    suspend fun getUserById(@Path("id") id: Long): Response<UserResponse>

    @PUT("api/users/{id}")
    suspend fun updateUser(
        @Path("id") id: Long,
        @Body request: UpdateUserRequest
    ): Response<UserResponse>

    @DELETE("api/users/{id}")
    suspend fun deleteUser(@Path("id") id: Long): Response<String>

    @POST("api/users")
    suspend fun createUser(@Body request: UserRequest): Response<UserResponse>

    @GET("api/users/profile/me")
    suspend fun getMyProfile(): Response<UserResponse>

    @PUT("api/users/profile/me")
    suspend fun updateMyProfile(@Body request: UpdateProfileRequest): Response<UserResponse>
}