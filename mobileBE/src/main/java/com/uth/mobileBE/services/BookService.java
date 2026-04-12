package com.uth.mobileBE.services;

import com.uth.mobileBE.dto.request.InitialBookRequest;
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

    @Transactional
    public InitialBookResponse createInitialBook(InitialBookRequest request) {
        Library library = libraryRepository.findById(request.getLibraryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thư viện"));
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thể loại"));


        Book book = Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .isbn(request.getIsbn())
                .basePrice(request.getBasePrice())
                .library(library)
                .category(category)
                .build();
        Book savedBook = bookRepository.save(book);

        BookCopy copy = BookCopy.builder()
                .book(savedBook)
                .barcode(request.getBarcode())
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

    public List<String> getLowCopyAlerts(Long libraryId) {
        List<String> titles = bookRepository.findBooksWithLowAvailableCopies(libraryId);
        return titles.stream()
                .map(title -> "Sách '" + title + "' có số lượng bản khả dụng dưới 2.")
                .collect(Collectors.toList());
    }
}
