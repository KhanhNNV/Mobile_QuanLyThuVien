package com.example.quanlythuvien.data.remote

import com.example.quanlythuvien.data.model.request.BookCopyRequest
import com.example.quanlythuvien.data.model.response.BookCopyResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface BookCopyApiService {
    @POST("api/book-copies")
    suspend fun createBookCopy(@Body request: BookCopyRequest): Response<BookCopyResponse>

    @GET("api/book-copies/book/{bookId}")
    suspend fun getBookCopiesByBook(@Path("bookId") bookId: Long): Response<List<BookCopyResponse>>

    @DELETE("api/book-copies/{copyId}")
    suspend fun deleteBookCopy(@Path("copyId") copyId: Long): Response<ResponseBody>
}
