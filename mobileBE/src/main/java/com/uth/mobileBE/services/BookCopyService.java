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
import org.springframework.dao.DataIntegrityViolationException;
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
        if (request.getBarcode() == null || request.getBarcode().isBlank()) {
            throw new RuntimeException("Barcode không được để trống");
        }
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sách với ID: " + request.getBookId()));

        if (bookCopyRepository.existsByBarcode(request.getBarcode().trim())) {
            throw new RuntimeException("Barcode đã tồn tại");
        }

        BookCopy copy = BookCopy.builder()
                .book(book)
                .barcode(request.getBarcode().trim())
                .condition(parseCondition(request.getCondition()))
                .status(parseStatus(request.getStatus()))
                .build();

        return mapToResponse(bookCopyRepository.save(copy));
    }

    public List<BookCopyResponse> getAllCopiesByBook(Long bookId) {
        if (!bookRepository.existsById(bookId)) {
            throw new RuntimeException("Không tìm thấy sách với ID: " + bookId);
        }
        return bookCopyRepository.findByBookId(bookId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public BookCopyResponse updateBookCopy(Long copyId, BookCopyRequest request) {
        BookCopy copy = bookCopyRepository.findById(copyId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bản sao với ID: " + copyId));

        if (request.getBarcode() != null && !request.getBarcode().isBlank()) {
            String trimmed = request.getBarcode().trim();
            if (bookCopyRepository.existsByBarcodeAndCopyIdNot(trimmed, copyId)) {
                throw new RuntimeException("Barcode đã tồn tại");
            }
            copy.setBarcode(trimmed);
        }

        if (request.getCondition() != null && !request.getCondition().isBlank()) {
            copy.setCondition(parseCondition(request.getCondition()));
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            copy.setStatus(parseStatus(request.getStatus()));
        }

        return mapToResponse(bookCopyRepository.save(copy));
    }

    @Transactional
    public void deleteBookCopy(Long copyId) {
        if (!bookCopyRepository.existsById(copyId)) {
            throw new RuntimeException("Không tìm thấy bản sao với ID: " + copyId);
        }
        try {
            bookCopyRepository.deleteById(copyId);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException(
                    "Không thể xóa bản sao vì đã gắn với phiếu mượn hoặc dữ liệu khác.", e);
        }
    }

    private ConditionBookCopy parseCondition(String raw) {
        if (raw == null || raw.isBlank()) {
            return ConditionBookCopy.NEW;
        }
        try {
            return ConditionBookCopy.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Giá trị condition không hợp lệ: " + raw);
        }
    }

    private StatusBookCopy parseStatus(String raw) {
        if (raw == null || raw.isBlank()) {
            return StatusBookCopy.AVAILABLE;
        }
        try {
            return StatusBookCopy.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Giá trị status không hợp lệ: " + raw);
        }
    }

    private BookCopyResponse mapToResponse(BookCopy copy) {
        return BookCopyResponse.builder()
                .copyId(copy.getCopyId())
                .bookId(copy.getBook() != null ? copy.getBook().getBookId() : null)
                .barcode(copy.getBarcode())
                .condition(copy.getCondition() != null ? copy.getCondition().name() : null)
                .status(copy.getStatus() != null ? copy.getStatus().name() : null)
                .build();
    }
}
