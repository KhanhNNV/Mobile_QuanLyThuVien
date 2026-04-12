package com.uth.mobileBE.services;

import com.uth.mobileBE.dto.request.LoanDetailRequest;
import com.uth.mobileBE.dto.response.LoanDetailResponse;
import com.uth.mobileBE.models.LoanDetail;
import com.uth.mobileBE.models.LoanDetailId;
import com.uth.mobileBE.repositories.BookCopyRepository;
import com.uth.mobileBE.repositories.LoanDetailRepository;
import com.uth.mobileBE.repositories.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LoanDetailService {

    @Autowired private LoanDetailRepository loanDetailRepository;
    @Autowired private LoanRepository loanRepository;
    @Autowired private BookCopyRepository bookCopyRepository;

    public List<LoanDetailResponse> getAllDetails() {
        return loanDetailRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public LoanDetailResponse createDetail(LoanDetailRequest request) {
        LoanDetailId id = new LoanDetailId(request.getLoanId(), request.getCopyId());

        var loan = loanRepository.findById(request.getLoanId())
                .orElseThrow(() -> new RuntimeException("Phiếu mượn không tồn tại"));
        var copy = bookCopyRepository.findById(request.getCopyId())
                .orElseThrow(() -> new RuntimeException("Bản sao sách không tồn tại"));

        LoanDetail detail = LoanDetail.builder()
                .id(id)
                .loan(loan)
                .bookCopy(copy)
                .dueDate(request.getDueDate())
                .status(request.getStatus())
                .penaltyAmount(0.0)
                .build();

        return mapToResponse(loanDetailRepository.save(detail));
    }

    @Transactional
    public LoanDetailResponse updateDetail(Long loanId, Long copyId, LoanDetailRequest request) {
        LoanDetailId id = new LoanDetailId(loanId, copyId);
        LoanDetail detail = loanDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết phiếu mượn"));

        // Chỉ cập nhật những trường không null từ request
        if (request.getDueDate() != null) detail.setDueDate(request.getDueDate());
        if (request.getReturnDate() != null) detail.setReturnDate(request.getReturnDate());
        if (request.getStatus() != null) detail.setStatus(request.getStatus());
        if (request.getPenaltyAmount() != null) detail.setPenaltyAmount(request.getPenaltyAmount());

        return mapToResponse(loanDetailRepository.save(detail));
    }

    public void deleteDetail(Long loanId, Long copyId) {
        loanDetailRepository.deleteById(new LoanDetailId(loanId, copyId));
    }


    public List<String> getDueTodayAlerts(Long libraryId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay(); // 00:00:00 hôm nay
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX); // 23:59:59 hôm nay

        List<LoanDetail> dueDetails = loanDetailRepository.findDueToday(libraryId, startOfDay, endOfDay);

        return dueDetails.stream()
                .map(ld -> "Phiếu mượn #" + ld.getLoan().getLoanId() +
                        " (Sách: " + ld.getBookCopy().getBook().getTitle() +
                        ") đến hạn trả ngay hôm nay!")
                .collect(Collectors.toList());
    }

    private LoanDetailResponse mapToResponse(LoanDetail detail) {
        return LoanDetailResponse.builder()
                .loanId(detail.getId().getLoanId())
                .copyId(detail.getId().getCopyId())
                .bookTitle(detail.getBookCopy().getBook().getTitle())
                .dueDate(detail.getDueDate())
                .returnDate(detail.getReturnDate())
                .status(detail.getStatus())
                .penaltyAmount(detail.getPenaltyAmount())
                .createdAt(detail.getCreatedAt())
                .updateAt(detail.getUpdateAt())
                .build();
    }
}