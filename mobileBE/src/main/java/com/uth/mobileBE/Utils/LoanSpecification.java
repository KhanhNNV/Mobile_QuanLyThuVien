package com.uth.mobileBE.Utils;

import com.uth.mobileBE.models.Loan;
import com.uth.mobileBE.models.LoanDetail;
import com.uth.mobileBE.models.enums.StatusLoan;
import com.uth.mobileBE.models.enums.StatusLoanDetail;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LoanSpecification {

    public static Specification<Loan> filterLoans(
            Long libraryId,
            String statusFilter,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            String searchQuery) {

        return (root, query, cb) -> {
            // Danh sách các điều kiện. Tất cả sẽ được nối với nhau bằng toán tử AND ở cuối.
            List<Predicate> predicates = new ArrayList<>();

            // 1. Lọc theo Library ID (Bắt buộc)
            predicates.add(cb.equal(root.get("library").get("libraryId"), libraryId));

            // 2. Lọc theo khoảng thời gian mượn (Kết hợp bằng AND)
            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("borrowDate"), fromDate));
            }
            if (toDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("borrowDate"), toDate));
            }

            // 3. Lọc theo từ khóa tìm kiếm (Kết hợp bằng AND)
            if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                String searchPattern = "%" + searchQuery.trim().toLowerCase() + "%";
                Predicate loanIdMatch = cb.like(root.get("loanId").as(String.class), searchPattern);
                Predicate readerNameMatch = cb.like(cb.lower(root.get("reader").get("fullName")), searchPattern);
                // Từ khóa có thể khớp ID hoặc Tên, nhưng cụm này vẫn là điều kiện AND với các tiêu chí khác
                predicates.add(cb.or(loanIdMatch, readerNameMatch));
            }

            // ====================================================================
            // 4. LỌC THEO TRẠNG THÁI VỚI ĐỘ ƯU TIÊN (VIOLATED > OVERDUE > ACTIVE/COMPLETED)
            // ====================================================================
            if (statusFilter != null && !statusFilter.trim().isEmpty()) {
                String filter = statusFilter.trim().toUpperCase();

                // --- Subquery 1: Kiểm tra xem có sách VI PHẠM (Lost/Damaged) không ---
                Subquery<Long> sqViolation = query.subquery(Long.class);
                Root<LoanDetail> rootViolation = sqViolation.from(LoanDetail.class);
                sqViolation.select(cb.literal(1L));
                sqViolation.where(cb.and(
                        cb.equal(rootViolation.get("loan"), root),
                        rootViolation.get("status").in(StatusLoanDetail.LOST, StatusLoanDetail.DAMAGED)
                ));
                Predicate hasViolation = cb.exists(sqViolation);

                // --- Subquery 2: Kiểm tra xem có sách QUÁ HẠN không ---
                // Quá hạn = Status là OVERDUE HOẶC (Status là BORROWING và Ngày hạn < Hiện tại)
                Subquery<Long> sqOverdue = query.subquery(Long.class);
                Root<LoanDetail> rootOverdue = sqOverdue.from(LoanDetail.class);
                sqOverdue.select(cb.literal(1L));
                Predicate isOverdueEnum = cb.equal(rootOverdue.get("status"), StatusLoanDetail.OVERDUE);
                Predicate isBorrowingPastDue = cb.and(
                        cb.equal(rootOverdue.get("status"), StatusLoanDetail.BORROWING),
                        cb.lessThan(rootOverdue.get("dueDate"), LocalDateTime.now())
                );
                sqOverdue.where(cb.and(
                        cb.equal(rootOverdue.get("loan"), root),
                        cb.or(isOverdueEnum, isBorrowingPastDue)
                ));
                Predicate hasOverdue = cb.exists(sqOverdue);


                // --- Xử lý logic ưu tiên dựa trên filter ---
                if ("VIOLATED".equals(filter)) {
                    // Trả về nếu: Có sách vi phạm HOẶC phiếu đã bị đánh dấu VIOLATED
                    predicates.add(cb.or(
                            hasViolation,
                            cb.equal(root.get("status"), StatusLoan.VIOLATED)
                    ));

                } else if ("OVERDUE".equals(filter)) {
                    // Trả về nếu: KHÔNG CÓ VI PHẠM (ưu tiên vi phạm) VÀ (Có sách quá hạn HOẶC phiếu bị đánh dấu OVERDUE)
                    Predicate isOverdueCondition = cb.or(
                            hasOverdue,
                            cb.equal(root.get("status"), StatusLoan.OVERDUE)
                    );
                    predicates.add(cb.and(cb.not(hasViolation), isOverdueCondition));

                } else if ("ACTIVE".equals(filter)) {
                    // Trả về nếu: KHÔNG VI PHẠM, KHÔNG QUÁ HẠN VÀ phiếu đang ACTIVE
                    predicates.add(cb.and(
                            cb.not(hasViolation),
                            cb.not(hasOverdue),
                            cb.equal(root.get("status"), StatusLoan.ACTIVE)
                    ));

                } else if ("COMPLETED".equals(filter)) {
                    // Trả về nếu: KHÔNG VI PHẠM, KHÔNG QUÁ HẠN VÀ phiếu đã COMPLETED
                    predicates.add(cb.and(
                            cb.not(hasViolation),
                            cb.not(hasOverdue),
                            cb.equal(root.get("status"), StatusLoan.COMPLETED)
                    ));
                }
            }

            // Gộp TẤT CẢ các điều kiện trong danh sách predicates lại bằng toán tử AND
            // Điều này đảm bảo nếu có cả thời gian, search và trạng thái, dữ liệu phải thỏa mãn TẤT CẢ.
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}