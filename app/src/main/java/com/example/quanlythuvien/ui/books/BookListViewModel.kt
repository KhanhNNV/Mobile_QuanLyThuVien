package com.example.quanlythuvien.ui.books

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quanlythuvien.data.model.request.BookCopyRequest
import com.example.quanlythuvien.data.model.request.BookRequest
import com.example.quanlythuvien.data.model.response.BookCopyResponse
import com.example.quanlythuvien.data.model.response.BookResponse
import com.example.quanlythuvien.data.model.response.CategoryResponse
import com.example.quanlythuvien.data.repository.BookCopyRepository
import com.example.quanlythuvien.data.repository.BookRepository
import com.example.quanlythuvien.data.repository.CategoryRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class BookListUiState {
    object Idle : BookListUiState()
    object Loading : BookListUiState()
    data class Success(val books: List<BookResponse>) : BookListUiState()
    data class Error(val message: String) : BookListUiState()
}

sealed class BookDetailUiState {
    object Idle : BookDetailUiState()
    object Loading : BookDetailUiState()
    data class Success(val book: BookResponse) : BookDetailUiState()
    data class Error(val message: String) : BookDetailUiState()
}

sealed class BookCopyUiState {
    object Idle : BookCopyUiState()
    object Loading : BookCopyUiState()
    data class Success(val bookId: Long, val copies: List<BookCopyResponse>) : BookCopyUiState()
    data class Error(val message: String) : BookCopyUiState()
}

class BookListViewModel(
    private val repository: BookRepository,
    private val categoryRepository: CategoryRepository,
    private val bookCopyRepository: BookCopyRepository
) : ViewModel() {

    private val _bookListState = MutableStateFlow<BookListUiState>(BookListUiState.Idle)
    val bookListState: StateFlow<BookListUiState> = _bookListState.asStateFlow()

    private val _categories = MutableStateFlow<List<CategoryResponse>>(emptyList())
    val categories: StateFlow<List<CategoryResponse>> = _categories.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _allBooks = MutableStateFlow<List<BookResponse>>(emptyList())
    private val _filteredBooks = MutableStateFlow<List<BookResponse>>(emptyList())
    val filteredBooks: StateFlow<List<BookResponse>> = _filteredBooks.asStateFlow()
    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    private val _bookDetailState = MutableStateFlow<BookDetailUiState>(BookDetailUiState.Idle)
    val bookDetailState: StateFlow<BookDetailUiState> = _bookDetailState.asStateFlow()
    private val _bookCopyState = MutableStateFlow<BookCopyUiState>(BookCopyUiState.Idle)
    val bookCopyState: StateFlow<BookCopyUiState> = _bookCopyState.asStateFlow()

    fun loadData() {
        loadCategories()
        loadBooks()
    }

    private fun loadBooks() {
        viewModelScope.launch {
            _bookListState.value = BookListUiState.Loading
            val result = repository.getBooksByLibrary()
            result
                .onSuccess { books ->
                    _allBooks.value = enrichBooksWithAvailableCopies(books)
                    applyFilters()
                    _bookListState.value = BookListUiState.Success(_allBooks.value)
                }
                .onFailure { error ->
                    _bookListState.value = BookListUiState.Error(error.message ?: "Tai danh sach sach that bai.")
                }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            runCatching { categoryRepository.getCategoriesByLibrary() }
                .onSuccess { response ->
                    if (response.isSuccessful) {
                        _categories.value = response.body().orEmpty()
                        applyFilters()
                    }
                }
        }
    }

    fun applyFilters(query: String = _searchQuery.value, categoryId: Long? = _selectedCategoryId.value) {
        _searchQuery.value = query
        _selectedCategoryId.value = categoryId
        val normalizedQuery = query.trim().lowercase()
        _filteredBooks.value = _allBooks.value
            .filter { book ->
                val matchesQuery = normalizedQuery.isEmpty() ||
                    book.title.lowercase().contains(normalizedQuery) ||
                    book.author.lowercase().contains(normalizedQuery) ||
                    book.isbn.lowercase().contains(normalizedQuery)

                val matchesCategory = categoryId == null || book.categoryId == categoryId

                matchesQuery && matchesCategory
            }
            .map { book ->
                val resolvedCategoryName = book.categoryName ?: categoryNameById(book.categoryId)
                if (resolvedCategoryName == null) {
                    book
                } else {
                    book.copy(categoryName = resolvedCategoryName)
                }
            }
    }

    fun categoryById(categoryId: Long?): CategoryResponse? {
        if (categoryId == null) {
            return null
        }
        return _categories.value.firstOrNull { category ->
            category.categoryId == categoryId
        }
    }

    fun categoryNameById(categoryId: Long?): String? {
        return categoryById(categoryId)?.name
    }

    fun resetFilters() {
        applyFilters(query = "", categoryId = null)
    }

    fun loadBookDetail(bookId: Long) {
        viewModelScope.launch {
            _bookDetailState.value = BookDetailUiState.Loading
            repository.getBookById(bookId)
                .onSuccess { book ->
                    _bookDetailState.value = BookDetailUiState.Success(book)
                }
                .onFailure { error ->
                    _bookDetailState.value =
                        BookDetailUiState.Error(error.message ?: "Không thể tải chi tiết sách.")
                }
        }
    }

    fun updateBook(
        bookId: Long,
        libraryId: Long,
        categoryId: Long,
        isbn: String,
        title: String,
        author: String,
        basePrice: Double
    ) {
        viewModelScope.launch {
            _bookDetailState.value = BookDetailUiState.Loading
            val request = BookRequest(
                libraryId = libraryId,
                categoryId = categoryId,
                isbn = isbn,
                title = title,
                author = author,
                basePrice = basePrice
            )
            repository.updateBook(bookId, request)
                .onSuccess {
                    loadBooks()
                    _bookDetailState.value = BookDetailUiState.Success(it)
                }
                .onFailure { error ->
                    _bookDetailState.value =
                        BookDetailUiState.Error(error.message ?: "Không thể cập nhật sách.")
                }
        }
    }

    fun loadBookCopies(bookId: Long) {
        viewModelScope.launch {
            _bookCopyState.value = BookCopyUiState.Loading
            bookCopyRepository.getBookCopiesByBook(bookId)
                .onSuccess { copies ->
                    _bookCopyState.value = BookCopyUiState.Success(bookId = bookId, copies = copies)
                }
                .onFailure { error ->
                    _bookCopyState.value = BookCopyUiState.Error(error.message ?: "Không thể tải bản sao sách.")
                }
        }
    }

    fun createBookCopy(bookId: Long, barcode: String, condition: String) {
        viewModelScope.launch {
            _bookCopyState.value = BookCopyUiState.Loading
            val request = BookCopyRequest(
                bookId = bookId,
                barcode = barcode,
                condition = condition,
                status = "AVAILABLE"
            )
            bookCopyRepository.createBookCopy(request)
                .onSuccess {
                    loadBookCopies(bookId)
                    loadBooks()
                }
                .onFailure { error ->
                    _bookCopyState.value = BookCopyUiState.Error(error.message ?: "Không thể thêm bản sao sách.")
                }
        }
    }

    fun deleteBookCopy(copyId: Long, bookId: Long) {
        viewModelScope.launch {
            _bookCopyState.value = BookCopyUiState.Loading
            bookCopyRepository.deleteBookCopy(copyId)
                .onSuccess {
                    loadBookCopies(bookId)
                    loadBooks()
                }
                .onFailure { error ->
                    _bookCopyState.value = BookCopyUiState.Error(error.message ?: "Không thể xóa bản sao sách.")
                }
        }
    }

    private suspend fun enrichBooksWithAvailableCopies(books: List<BookResponse>): List<BookResponse> {
        val deferred = books.map { book ->
            viewModelScope.async {
                val availableCount = bookCopyRepository
                    .getBookCopiesByBook(book.bookId)
                    .getOrNull()
                    ?.count { it.status.equals("AVAILABLE", ignoreCase = true) }
                if (availableCount == null) {
                    book
                } else {
                    book.copy(availableCopies = availableCount)
                }
            }
        }
        return deferred.map { it.await() }
    }
}