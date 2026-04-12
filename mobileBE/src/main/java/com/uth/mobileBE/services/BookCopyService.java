package com.uth.mobileBE.services;

import com.uth.mobileBE.dto.request.BookCopyRequest;
import com.uth.mobileBE.dto.response.BookCopyResponse;
import com.uth.mobileBE.models.Book;
import com.uth.mobileBE.models.BookCopy;
import com.uth.mobileBE.models.enums.ConditionBookCopy;
import com.uth.mobileBE.models.enums.StatusBookCopy;
import com.uth.mobileBE.repositories.BookCopyRepository;
import com.uth.mobileBE.repositories.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookCopyService {

    private final BookCopyRepository bookCopyRepository;
    private final BookRepository bookRepository;

    @Transactional
    public BookCopyResponse createBookCopy(BookCopyRequest request) {
        if (request.getBookId() == null) {
            throw new RuntimeException("bookId không được để trống");
        }
        if (request.getBarcode() == null || request.getBarcode().isBlank()) {
            throw new RuntimeException("Barcode không được để trống");
        }

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sách với ID: " + request.getBookId()));

        if (bookCopyRepository.existsByBarcode(request.getBarcode())) {
            throw new RuntimeException("Mã barcode đã tồn tại!");
        }

        ConditionBookCopy condition = request.getCondition() != null
                ? request.getCondition()
                : ConditionBookCopy.NEW;
        StatusBookCopy status = request.getStatus() != null
                ? request.getStatus()
                : StatusBookCopy.AVAILABLE;

        BookCopy copy = BookCopy.builder()
                .book(book)
                .barcode(request.getBarcode())
                .condition(condition)
                .status(status)
                .build();

        BookCopy saved = bookCopyRepository.save(copy);
        return mapToResponse(saved);
    }

    public List<BookCopyResponse> getBookCopiesByBook(Long bookId) {
        if (!bookRepository.existsById(bookId)) {
            throw new RuntimeException("Không tìm thấy sách với ID: " + bookId);
        }
        return bookCopyRepository.findByBook_BookId(bookId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public BookCopyResponse getBookCopyById(Long copyId) {
        BookCopy copy = bookCopyRepository.findById(copyId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bản sao với ID: " + copyId));
        return mapToResponse(copy);
    }

    @Transactional
    public BookCopyResponse updateBookCopy(Long copyId, BookCopyRequest request) {
        if (request.getBarcode() == null || request.getBarcode().isBlank()) {
            throw new RuntimeException("Barcode không được để trống");
        }
        if (request.getCondition() == null || request.getStatus() == null) {
            throw new RuntimeException("condition và status không được để trống");
        }

        BookCopy copy = bookCopyRepository.findById(copyId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bản sao với ID: " + copyId));

        if (!copy.getBarcode().equals(request.getBarcode())
                && bookCopyRepository.existsByBarcode(request.getBarcode())) {
            throw new RuntimeException("Mã barcode đã tồn tại!");
        }

        copy.setBarcode(request.getBarcode());
        copy.setCondition(request.getCondition());
        copy.setStatus(request.getStatus());

        BookCopy updated = bookCopyRepository.save(copy);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteBookCopy(Long copyId) {
        if (!bookCopyRepository.existsById(copyId)) {
            throw new RuntimeException("Không tìm thấy bản sao với ID: " + copyId);
        }
        bookCopyRepository.deleteById(copyId);
    }

    private BookCopyResponse mapToResponse(BookCopy copy) {
        return BookCopyResponse.builder()
                .copyId(copy.getCopyId())
                .bookId(copy.getBook() != null ? copy.getBook().getBookId() : null)
                .barcode(copy.getBarcode())
                .condition(copy.getCondition())
                .status(copy.getStatus())
                .createdAt(copy.getCreatedAt())
                .updatedAt(copy.getUpdatedAt())
                .build();
    }
}
