package com.example.quanlythuvien.data.remote

import com.example.quanlythuvien.data.model.request.BookRequest
import com.example.quanlythuvien.data.model.request.InitialBookRequest
import com.example.quanlythuvien.data.model.response.BookResponse
import com.example.quanlythuvien.data.model.response.InitialBookResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.PUT

private const val BOOK_ENDPOINT = "api/books"
interface BookApiService {
    @POST("$BOOK_ENDPOINT/welcome")
    suspend fun createInitialBook(@Body request: InitialBookRequest): Response<InitialBookResponse>

    @GET("$BOOK_ENDPOINT/count")
    suspend fun countBooksByLibrary(): Response<Long>

    @GET("$BOOK_ENDPOINT/alerts/low-copies")
    suspend fun getLowCopyAlerts(): Response<List<String>>

    @GET("$BOOK_ENDPOINT/library/{libraryId}")
    suspend fun getBooksByLibrary(@Path("libraryId") libraryId: Long): Response<List<BookResponse>>

    @GET("$BOOK_ENDPOINT/{bookId}")
    suspend fun getBookById(@Path("bookId") bookId: Long): Response<BookResponse>

    @PUT("$BOOK_ENDPOINT/{bookId}")
    suspend fun updateBook(
        @Path("bookId") bookId: Long,
        @Body request: BookRequest
    ): Response<BookResponse>

    @POST("api/books")
    suspend fun createBook(@Body request: BookRequest): Response<BookResponse>
}