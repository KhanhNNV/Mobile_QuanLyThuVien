package com.uth.mobileBE.services;

import com.uth.mobileBE.dto.request.BookRequest;
import com.uth.mobileBE.dto.request.InitialBookRequest;
import com.uth.mobileBE.dto.response.BookResponse;
import com.uth.mobileBE.dto.response.InitialBookResponse;
import com.uth.mobileBE.models.Book;
import com.uth.mobileBE.models.BookCopy;
import com.uth.mobileBE.models.Category;
import com.uth.mobileBE.models.Library;
import com.uth.mobileBE.models.enums.ConditionBookCopy;
import com.uth.mobileBE.models.enums.StatusBookCopy;
import com.uth.mobileBE.repositories.BookCopyRepository;
import com.uth.mobileBE.repositories.BookRepository;
import com.uth.mobileBE.repositories.CategoryRepository;
import com.uth.mobileBE.repositories.LibraryRepository;
import com.uth.mobileBE.repositories.LoanDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;
    private final LibraryRepository libraryRepository;
    private final CategoryRepository categoryRepository;
    private final LoanDetailRepository loanDetailRepository;

    @Transactional
    public InitialBookResponse createInitialBook(InitialBookRequest request) {
        Library library = libraryRepository.findById(request.getLibraryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thư viện"));
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thể loại"));

        assertCategoryBelongsToLibrary(category, library);

        if (request.getIsbn() != null && !request.getIsbn().isBlank()
                && bookRepository.existsByIsbnAndLibrary_LibraryId(request.getIsbn().trim(), library.getLibraryId())) {
            throw new RuntimeException("ISBN đã tồn tại trong thư viện này");
        }

        Book book = Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .isbn(request.getIsbn())
                .basePrice(request.getBasePrice())
                .library(library)
                .category(category)
                .build();
        Book savedBook = bookRepository.save(book);

        if (request.getBarcode() == null || request.getBarcode().isBlank()) {
            throw new RuntimeException("Barcode không được để trống");
        }
        if (bookCopyRepository.existsByBarcode(request.getBarcode().trim())) {
            throw new RuntimeException("Barcode đã tồn tại");
        }

        BookCopy copy = BookCopy.builder()
                .book(savedBook)
                .barcode(request.getBarcode().trim())
                .condition(ConditionBookCopy.NEW)
                .status(StatusBookCopy.AVAILABLE)
                .build();
        bookCopyRepository.save(copy);

        return InitialBookResponse.builder()
                .bookId(savedBook.getBookId())
                .title(savedBook.getTitle())
                .author(savedBook.getAuthor())
                .isbn(savedBook.getIsbn())
                .basePrice(savedBook.getBasePrice())
                .libraryId(library.getLibraryId())
                .categoryId(category.getCategoryId())
                .copyId(copy.getCopyId())
                .barcode(copy.getBarcode())
                .condition(copy.getCondition().name())
                .status(copy.getStatus().name())
                .build();
    }

    @Transactional
    public BookResponse createBook(BookRequest request) {
        Library library = libraryRepository.findById(request.getLibraryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thư viện với ID: " + request.getLibraryId()));
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thể loại với ID: " + request.getCategoryId()));

        assertCategoryBelongsToLibrary(category, library);

        if (request.getIsbn() != null && !request.getIsbn().isBlank()
                && bookRepository.existsByIsbnAndLibrary_LibraryId(request.getIsbn().trim(), library.getLibraryId())) {
            throw new RuntimeException("ISBN đã tồn tại trong thư viện này");
        }

        Book book = Book.builder()
                .library(library)
                .category(category)
                .isbn(request.getIsbn())
                .title(request.getTitle())
                .author(request.getAuthor())
                .basePrice(request.getBasePrice())
                .build();

        Book saved = bookRepository.save(book);
        return mapBookToResponse(saved);
    }

    public List<BookResponse> getAllBooksByLibrary(Long libraryId) {
        return bookRepository.findByLibraryId(libraryId).stream()
                .map(this::mapBookToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public BookResponse updateBook(Long bookId, BookRequest request) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sách với ID: " + bookId));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thể loại với ID: " + request.getCategoryId()));

        Library library = book.getLibrary();
        assertCategoryBelongsToLibrary(category, library);

        if (request.getIsbn() != null && !request.getIsbn().isBlank()
                && bookRepository.existsByIsbnAndLibrary_LibraryIdAndBookIdNot(
                request.getIsbn().trim(), library.getLibraryId(), bookId)) {
            throw new RuntimeException("ISBN đã tồn tại trong thư viện này");
        }

        book.setIsbn(request.getIsbn());
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setBasePrice(request.getBasePrice());
        book.setCategory(category);

        return mapBookToResponse(bookRepository.save(book));
    }

    @Transactional
    public void deleteBook(Long bookId) {
        if (!bookRepository.existsById(bookId)) {
            throw new RuntimeException("Không tìm thấy sách với ID: " + bookId);
        }
        if (loanDetailRepository.existsByBookCopy_Book_BookId(bookId)) {
            throw new RuntimeException("Không thể xóa sách vì đã có bản sao gắn với phiếu mượn");
        }
        List<BookCopy> copies = bookCopyRepository.findByBookId(bookId);
        bookCopyRepository.deleteAll(copies);
        bookRepository.deleteById(bookId);
    }

    private void assertCategoryBelongsToLibrary(Category category, Library library) {
        if (category.getLibrary() == null
                || !category.getLibrary().getLibraryId().equals(library.getLibraryId())) {
            throw new RuntimeException("Thể loại không thuộc thư viện đã chọn");
        }
    }

    private BookResponse mapBookToResponse(Book book) {
        return BookResponse.builder()
                .bookId(book.getBookId())
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .author(book.getAuthor())
                .basePrice(book.getBasePrice())
                .libraryId(book.getLibrary() != null ? book.getLibrary().getLibraryId() : null)
                .categoryId(book.getCategory() != null ? book.getCategory().getCategoryId() : null)
                .build();
    }
}
