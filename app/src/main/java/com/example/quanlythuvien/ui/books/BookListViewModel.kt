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

    private val currentLibraryIdState = MutableStateFlow<Long?>(null)

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
        viewModelScope.launch {
            loadCategories()
            loadBooks()
        }
    }

    private fun loadBooks() {
        viewModelScope.launch {
            _bookListState.value = BookListUiState.Loading
            val result = repository.getBooksByLibrary()
            result
                .onSuccess { books ->
                    _allBooks.value = books
                    applyFilters()
                    _bookListState.value = BookListUiState.Success(_allBooks.value)
                    refreshAllAvailableCopies(books)
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
        categoryId: Long,
        isbn: String,
        title: String,
        author: String,
        basePrice: Double
    ) {
        viewModelScope.launch {
            _bookDetailState.value = BookDetailUiState.Loading
            val request = BookRequest(
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

    fun buildNextBarcode(bookId: Long, categoryName: String?): String {
        val prefix = normalizeCategoryCode(categoryName)
        val sameBookCopies = _bookCopyState.value.let { state ->
            if (state is BookCopyUiState.Success && state.bookId == bookId) state.copies else emptyList()
        }
        val nextSequence = sameBookCopies.size + 1
        return "$prefix-$bookId-$nextSequence"
    }

    private fun updateBookAvailableCopies(bookId: Long, copies: List<BookCopyResponse>) {
        val availableCount = copies.count { it.status.equals("AVAILABLE", ignoreCase = true) }
        _allBooks.value = _allBooks.value.map { book ->
            if (book.bookId == bookId) book.copy(availableCopies = availableCount) else book
        }
        applyFilters()
    }

    fun refreshAvailableCopiesFromBookDetail(bookId: Long) {
        viewModelScope.launch {
            bookCopyRepository.getBookCopiesByBook(bookId)
                .onSuccess { copies -> updateBookAvailableCopies(bookId, copies) }
        }
    }

    private fun refreshAllAvailableCopies(books: List<BookResponse>) {
        viewModelScope.launch {
            val results = books.map { book ->
                async {
                    bookCopyRepository.getBookCopiesByBook(book.bookId)
                        .onSuccess { copies -> updateBookAvailableCopies(book.bookId, copies) }
                }
            }
            results.forEach { it.await() }
        }
    }

    private fun normalizeCategoryCode(categoryName: String?): String {
        val ascii = removeDiacritics(categoryName.orEmpty())
            .uppercase()
            .replace(Regex("[^A-Z0-9 ]"), " ")
            .trim()

        return when {
            ascii.contains("VAN HOC") || ascii.contains("VANHOC") -> "VH"
            ascii.contains("KY NANG SONG") || ascii.contains("KYNANG SONG") -> "KNS"
            ascii.contains("KINH DOANH") || ascii.contains("KINHDOANH") -> "KD"
            ascii.contains("CONG NGHE THONG TIN") || ascii.contains("CONGNGHETHONGTIN") -> "CNTT"
            ascii.contains("LAP TRINH") || ascii.contains("LAPTRINH") -> "LT"
            ascii.contains("NGOAI NGU") || ascii.contains("NGOAINGU") -> "NN"
            ascii.contains("THIEU NHI") || ascii.contains("THIEUNHI") -> "TN"
            ascii.contains("TRINH THAM") || ascii.contains("TRINHTHAM") -> "TT"
            else -> ascii.split(" ").filter { it.isNotBlank() }.joinToString("") { it.take(1) }.ifBlank { "BK" }
        }
    }

    private fun removeDiacritics(input: String): String {
        return input
            .replace('á', 'a').replace('à', 'a').replace('ả', 'a').replace('ã', 'a').replace('ạ', 'a')
            .replace('ă', 'a').replace('ắ', 'a').replace('ằ', 'a').replace('ẳ', 'a').replace('ẵ', 'a').replace('ặ', 'a')
            .replace('â', 'a').replace('ấ', 'a').replace('ầ', 'a').replace('ẩ', 'a').replace('ẫ', 'a').replace('ậ', 'a')
            .replace('é', 'e').replace('è', 'e').replace('ẻ', 'e').replace('ẽ', 'e').replace('ẹ', 'e')
            .replace('ê', 'e').replace('ế', 'e').replace('ề', 'e').replace('ể', 'e').replace('ễ', 'e').replace('ệ', 'e')
            .replace('í', 'i').replace('ì', 'i').replace('ỉ', 'i').replace('ĩ', 'i').replace('ị', 'i')
            .replace('ó', 'o').replace('ò', 'o').replace('ỏ', 'o').replace('õ', 'o').replace('ọ', 'o')
            .replace('ô', 'o').replace('ố', 'o').replace('ồ', 'o').replace('ổ', 'o').replace('ỗ', 'o').replace('ộ', 'o')
            .replace('ơ', 'o').replace('ớ', 'o').replace('ờ', 'o').replace('ở', 'o').replace('ỡ', 'o').replace('ợ', 'o')
            .replace('ú', 'u').replace('ù', 'u').replace('ủ', 'u').replace('ũ', 'u').replace('ụ', 'u')
            .replace('ư', 'u').replace('ứ', 'u').replace('ừ', 'u').replace('ử', 'u').replace('ữ', 'u').replace('ự', 'u')
            .replace('ý', 'y').replace('ỳ', 'y').replace('ỷ', 'y').replace('ỹ', 'y').replace('ỵ', 'y')
            .replace('đ', 'd')
            .replace('Á', 'A').replace('À', 'A').replace('Ả', 'A').replace('Ã', 'A').replace('Ạ', 'A')
            .replace('Ă', 'A').replace('Ắ', 'A').replace('Ằ', 'A').replace('Ẳ', 'A').replace('Ẵ', 'A').replace('Ặ', 'A')
            .replace('Â', 'A').replace('Ấ', 'A').replace('Ầ', 'A').replace('Ẩ', 'A').replace('Ẫ', 'A').replace('Ậ', 'A')
            .replace('É', 'E').replace('È', 'E').replace('Ẻ', 'E').replace('Ẽ', 'E').replace('Ẹ', 'E')
            .replace('Ê', 'E').replace('Ế', 'E').replace('Ề', 'E').replace('Ể', 'E').replace('Ễ', 'E').replace('Ệ', 'E')
            .replace('Í', 'I').replace('Ì', 'I').replace('Ỉ', 'I').replace('Ĩ', 'I').replace('Ị', 'I')
            .replace('Ó', 'O').replace('Ò', 'O').replace('Ỏ', 'O').replace('Õ', 'O').replace('Ọ', 'O')
            .replace('Ô', 'O').replace('Ố', 'O').replace('Ồ', 'O').replace('Ổ', 'O').replace('Ỗ', 'O').replace('Ộ', 'O')
            .replace('Ơ', 'O').replace('Ớ', 'O').replace('Ờ', 'O').replace('Ở', 'O').replace('Ỡ', 'O').replace('Ợ', 'O')
            .replace('Ú', 'U').replace('Ù', 'U').replace('Ủ', 'U').replace('Ũ', 'U').replace('Ụ', 'U')
            .replace('Ư', 'U').replace('Ứ', 'U').replace('Ừ', 'U').replace('Ử', 'U').replace('Ữ', 'U').replace('Ự', 'U')
            .replace('Ý', 'Y').replace('Ỳ', 'Y').replace('Ỷ', 'Y').replace('Ỹ', 'Y').replace('Ỵ', 'Y')
            .replace('Đ', 'D')
    }

    fun updateBookCopy(copyId: Long, bookId: Long, condition: String, barcode: String? = null) {
        viewModelScope.launch {
            _bookCopyState.value = BookCopyUiState.Loading
            val currentCopy = (_bookCopyState.value as? BookCopyUiState.Success)
                ?.copies
                ?.firstOrNull { it.copyId == copyId }
            val request = BookCopyRequest(
                bookId = currentCopy?.bookId ?: bookId,
                barcode = barcode ?: currentCopy?.barcode.orEmpty(),
                condition = condition,
                status = currentCopy?.status ?: "AVAILABLE"
            )
            bookCopyRepository.updateBookCopy(copyId, request)
                .onSuccess {
                    loadBookCopies(bookId)
                    loadBooks()
                }
                .onFailure { error ->
                    _bookCopyState.value = BookCopyUiState.Error(error.message ?: "Không thể cập nhật bản sao sách.")
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

}
