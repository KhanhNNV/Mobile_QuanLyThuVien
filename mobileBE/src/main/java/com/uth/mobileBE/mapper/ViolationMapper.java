package com.uth.mobileBE.mapper;

import com.uth.mobileBE.dto.response.ViolationResponse;
import com.uth.mobileBE.models.Violation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ViolationMapper {

    public ViolationResponse toResponse(Violation violation) {
        if (violation == null) return null;

        // Lấy thông tin sách từ Loan hoặc LoanDetail
        String bookTitle = null;
        Long bookId = null;

        if (violation.getLoanDetail() != null &&
                violation.getLoanDetail().getBookCopy() != null &&
                violation.getLoanDetail().getBookCopy().getBook() != null) {
            bookTitle = violation.getLoanDetail().getBookCopy().getBook().getTitle();
            bookId = violation.getLoanDetail().getBookCopy().getBook().getBookId();
        }

        return ViolationResponse.builder()
                .violationId(violation.getViolationId())
                .readerId(violation.getReader() != null ? violation.getReader().getReaderId() : null)
                .readerName(violation.getReader() != null ? violation.getReader().getFullName() : null)
                .barcode(violation.getReader() != null ? violation.getReader().getBarcode() : null)
                .reason(violation.getReason())
                .status(violation.getStatus().toString())
                .bookId(bookId)
                .bookTitle(bookTitle)
                .loanId(violation.getLoan() != null ? violation.getLoan().getLoanId() : null)
                .loanDetailId(violation.getLoanDetail() != null ? violation.getLoanDetail().getLoanDetailId() : null)
                .createdAt(violation.getCreatedAt())
                .updatedAt(violation.getUpdatedAt())
                .build();
    }

    public List<ViolationResponse> toResponseList(List<Violation> violations) {
        return violations.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}