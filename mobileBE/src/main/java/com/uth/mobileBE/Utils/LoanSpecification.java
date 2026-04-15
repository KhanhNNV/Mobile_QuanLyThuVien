//package com.uth.mobileBE.Utils;
//
//import com.uth.mobileBE.models.Loan;
//import com.uth.mobileBE.models.LoanDetail;
//import com.uth.mobileBE.models.enums.StatusLoan;
//import com.uth.mobileBE.models.enums.StatusLoanDetail;
//import jakarta.persistence.criteria.*;
//import org.springframework.data.jpa.domain.Specification;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//
//
////Lớp này đảm nhiệm chức năng xử lý dữ liệu lọc
//public class LoanSpecification {
//
//    public static Specification<Loan> filterLoans(
//            Long libraryId,
//            String statusFilter,
//            LocalDateTime fromDate,
//            LocalDateTime toDate,
//            String searchQuery) {
//
//        return (root, query, cb) -> {
//            List<Predicate> predicates = new ArrayList<>();
//
//            // 1. Lọc theo Library ID
//            predicates.add(cb.equal(root.get("library").get("libraryId"), libraryId));
//
//            // 2. Lọc theo khoảng thời gian mượn
//            if (fromDate != null) {
//                predicates.add(cb.greaterThanOrEqualTo(root.get("borrowDate"), fromDate));
//            }
//            if (toDate != null) {
//                predicates.add(cb.lessThanOrEqualTo(root.get("borrowDate"), toDate));
//            }
//
//            // 3. Lọc theo từ khóa tìm kiếm
//            if (searchQuery != null && !searchQuery.trim().isEmpty()) {
//                String searchPattern = "%" + searchQuery.trim().toLowerCase() + "%";
//                Predicate loanIdMatch = cb.like(root.get("loanId").as(String.class), searchPattern);
//                Predicate readerNameMatch = cb.like(cb.lower(root.get("reader").get("fullName")), searchPattern);
//                predicates.add(cb.or(loanIdMatch, readerNameMatch));
//            }
//
//            // ====================================================================
//            // 4. LỌC THEO TRẠNG THÁI "THỰC TẾ" (Sử dụng đúng Enum mới)
//            // ====================================================================
//            if (statusFilter != null && !statusFilter.trim().isEmpty()) {
//                if ("RETURNED".equalsIgnoreCase(statusFilter)) {
//                    // Nếu cần tìm phiếu đã hoàn tất: Lọc theo StatusLoan.RETURNED
//                    predicates.add(cb.equal(root.get("status"), StatusLoan.RETURNED));
//                } else {
//                    // Subquery kiểm tra chi tiết sách
//                    Subquery<Long> subquery = query.subquery(Long.class);
//                    Root<LoanDetail> detailRoot = subquery.from(LoanDetail.class);
//                    subquery.select(cb.literal(1L));
//
//                    // Định nghĩa "Sách quá hạn": Đang ở trạng thái BORROWING (chưa trả) VÀ Ngày hạn < Hiện tại
//                    Predicate isBorrowing = cb.equal(detailRoot.get("status"), StatusLoanDetail.BORROWING);
//                    Predicate isPastDue = cb.lessThan(detailRoot.get("dueDate"), LocalDateTime.now());
//                    Predicate belongsToLoan = cb.equal(detailRoot.get("loan"), root);
//
//                    subquery.where(cb.and(isBorrowing, isPastDue, belongsToLoan));
//
//                    // hasOverdueBooks = TRUE nếu subquery tìm thấy ít nhất 1 cuốn sách thỏa mãn điều kiện trên
//                    Predicate hasOverdueBooks = cb.exists(subquery);
//
//                    if ("OVERDUE".equalsIgnoreCase(statusFilter)) {
//                        // Lọc phiếu QUÁ HẠN: Phiếu chưa chốt RETURNED và CÓ sách quá hạn
//                        predicates.add(cb.and(
//                                cb.notEqual(root.get("status"), StatusLoan.RETURNED),
//                                hasOverdueBooks
//                        ));
//                    } else if ("BORROWING".equalsIgnoreCase(statusFilter)) {
//                        // Lọc phiếu ĐANG MƯỢN (trong hạn): Phiếu chưa chốt RETURNED và KHÔNG CÓ sách nào quá hạn
//                        predicates.add(cb.and(
//                                cb.notEqual(root.get("status"), StatusLoan.RETURNED),
//                                cb.not(hasOverdueBooks)
//                        ));
//                    }
//                }
//            }
//
//            return cb.and(predicates.toArray(new Predicate[0]));
//        };
//    }
//}