package com.uth.mobileBE.services;

import com.uth.mobileBE.dto.request.BookRequest;
import com.uth.mobileBE.dto.response.BookResponse;
import com.uth.mobileBE.models.Book;
import com.uth.mobileBE.models.BookCopy;
import com.uth.mobileBE.models.Category;
import com.uth.mobileBE.models.Library;
import com.uth.mobileBE.repositories.BookCopyRepository;
import com.uth.mobileBE.repositories.BookRepository;
import com.uth.mobileBE.repositories.CategoryRepository;
import com.uth.mobileBE.repositories.LibraryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookCrudService {

    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;
    private final LibraryRepository libraryRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public BookResponse createBook(BookRequest request) {
        Library library = libraryRepository.findById(request.getLibraryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thư viện với ID: " + request.getLibraryId()));
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thể loại với ID: " + request.getCategoryId()));

        assertCategoryBelongsToLibrary(category, library);

        if (request.getIsbn() != null && !request.getIsbn().isBlank()
                && bookRepository.existsByIsbnAndLibrary_LibraryId(request.getIsbn(), library.getLibraryId())) {
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
        return mapToResponse(saved);
    }

    public List<BookResponse> getBooksByLibrary(Long libraryId) {
        List<Book> books = bookRepository.findByLibrary_LibraryId(libraryId);
        return books.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public BookResponse getBookById(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sách với ID: " + bookId));
        return mapToResponse(book);
    }

    public Long countAllBookByLibrary(Long libraryId) {
        return bookRepository.countByLibrary_LibraryId(libraryId);
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
                request.getIsbn(), library.getLibraryId(), bookId)) {
            throw new RuntimeException("ISBN đã tồn tại trong thư viện này");
        }

        book.setIsbn(request.getIsbn());
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setBasePrice(request.getBasePrice());
        book.setCategory(category);

        Book updated = bookRepository.save(book);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteBook(Long bookId) {
        bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sách với ID: " + bookId));

        List<BookCopy> copies = bookCopyRepository.findByBook_BookId(bookId);
        bookCopyRepository.deleteAll(copies);
        bookRepository.deleteById(bookId);
    }

    private void assertCategoryBelongsToLibrary(Category category, Library library) {
        if (category.getLibrary() == null
                || !category.getLibrary().getLibraryId().equals(library.getLibraryId())) {
            throw new RuntimeException("Thể loại không thuộc thư viện này");
        }
    }

    private BookResponse mapToResponse(Book book) {
        return BookResponse.builder()
                .bookId(book.getBookId())
                .libraryId(book.getLibrary() != null ? book.getLibrary().getLibraryId() : null)
                .categoryId(book.getCategory() != null ? book.getCategory().getCategoryId() : null)
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .author(book.getAuthor())
                .basePrice(book.getBasePrice())
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .build();
    }
}
