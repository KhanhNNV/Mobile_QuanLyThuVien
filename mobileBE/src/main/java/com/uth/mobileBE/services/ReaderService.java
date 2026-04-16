package com.uth.mobileBE.services;

import com.uth.mobileBE.Utils.SecurityUtils;
import com.uth.mobileBE.dto.request.ReaderRequest;
import com.uth.mobileBE.dto.request.RenewReaderMembershipRequest;
import com.uth.mobileBE.dto.response.ReaderResponse;
import com.uth.mobileBE.models.Library;
import com.uth.mobileBE.models.Reader;
import com.uth.mobileBE.models.User;
import com.uth.mobileBE.models.enums.Role;
import com.uth.mobileBE.models.enums.StatusLibrary;
import com.uth.mobileBE.repositories.LibraryRepository;
import com.uth.mobileBE.repositories.ReaderRepository;
import com.uth.mobileBE.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReaderService {
    private final ReaderRepository readerRepository;
    private final LibraryRepository libraryRepository;
    private final UserRepository userRepository;

    //Tạo người độc giả
    @Transactional
    public ReaderResponse createReader(ReaderRequest request) {
        Long libraryId = SecurityUtils.getLibraryId();
        Library libraryRef = libraryRepository.getReferenceById(libraryId);
        Reader reader = Reader.builder()
                              .fullName(request.getFullName())
                              .phone(request.getPhone())
                              .barcode(request.getBarcode())
                              .membershipExpiry(request.getMembershipExpiry())
                              .library(libraryRef)
                              .isBlocked(false)
                              .build();

        Reader saved = readerRepository.save(reader);
        return mapToReaderResponse(saved);
    }

    public List<ReaderResponse> getAllReaders() {
        return readerRepository.findAll().stream()
                               .map(this::mapToReaderResponse)
                               .collect(Collectors.toList());
    }
    /**
     * Lấy danh sách độc giả theo trang
     * @param `page` Số thứ tự trang (bắt đầu từ 0)
     * @param `size` Số lượng phần tử trên 1 trang (ví dụ: 10)
     */
    public Page<ReaderResponse> getReadersPaginated(int page, int size) {
        Long libraryId = SecurityUtils.getLibraryId();
        Pageable pageable = PageRequest.of(page, size);
        return readerRepository.findByLibrary_LibraryId(libraryId, pageable)
                               .map(this::mapToReaderResponse);

    }
    //Tìm lọc độc giả
    public ReaderResponse getReaderById(Long id) {
        Reader reader = readerRepository.findById(id)
                                        .orElseThrow(() -> new RuntimeException("Không tìm thấy độc giả"));
        return mapToReaderResponse(reader);
    }

    /**Search reader
     * @param `fullName`, `phone`, `barcode`
     * @return listReader
     */
    public List<ReaderResponse> searchListReader(String request) {
        if (request == null || request.trim().isEmpty()) {
            return Collections.emptyList();
        }
        Long libraryId = SecurityUtils.getLibraryId();
        List<Reader> listReader = readerRepository.searchReadersByLibraryId(libraryId, request.trim());
        return listReader.stream().map(reader -> mapToReaderResponse(reader))
                                   .collect(Collectors.toList());
    }

    /**
     * Cập nhật thông tin độc giả
     * @param `id`       ID của độc giả cần cập nhật
     * @param `request`  Đối tượng chứa các trường cần cập nhật: fullName, phone, membershipExpiry
     * @return ReaderResponse sau khi cập nhật
     */
    @Transactional
    public ReaderResponse updateReader(Long id, ReaderRequest request) {
        Long libraryId = SecurityUtils.getLibraryId();
        Reader reader = readerRepository.findById(id)
                                        .orElseThrow(() -> new RuntimeException("Không tìm thấy độc giả để cập nhật"));


        //Kiểm tra độc giả này có phi thuộc thư viện này
        if (!reader.getLibrary().getLibraryId().equals(libraryId)) {
            throw new RuntimeException("Từ chối truy cập: Bạn không có quyền chỉnh sửa độc giả của thư viện khác.");
        }
        // Cập nhật các trường thông tin
        reader.setFullName(request.getFullName());
        reader.setPhone(request.getPhone());
        //reader.setBarcode(request.getBarcode());
        reader.setMembershipExpiry(request.getMembershipExpiry());

        Reader updated = readerRepository.save(reader);
        return mapToReaderResponse(updated);
    }

    /**
     * Delete reader
     * @param `id`
     * @return void
     */
    @Transactional
    public void deleteReader(Long id) {
        Long libraryId = SecurityUtils.getLibraryId();
        Reader reader = readerRepository.findById(id)
                                        .orElseThrow(() -> new RuntimeException("Không tìm thấy độc giả để xóa"));


        //Kiểm tra độc giả này có phi thuộc thư viện này
        if (!reader.getLibrary().getLibraryId().equals(libraryId)) {
            throw new RuntimeException("Từ chối truy cập: Bạn không có quyền chỉnh sửa độc giả của thư viện khác.");
        }

        readerRepository.deleteById(id);
    }

    public Long countReaders(Long libraryId) {
        return readerRepository.countByLibrary_LibraryId(libraryId);
    }



    @Transactional
    public ReaderResponse renewMembership(Long pathReaderId, RenewReaderMembershipRequest request) {
        Long currentLibraryId = SecurityUtils.getLibraryId();
        String currentUsername = SecurityUtils.getUsername();

        User sender = userRepository.findByUsername(currentUsername)
                                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng hiện tại"));

        // 1) Verify senderId gửi lên phải khớp user đang đăng nhập
        if (request.getSenderId() == null || !request.getSenderId().equals(sender.getUserId())) {
            throw new RuntimeException("senderId không hợp lệ");
        }

        // 2) Verify sender thuộc đúng thư viện theo token
        if (sender.getLibrary() == null || !currentLibraryId.equals(sender.getLibrary().getLibraryId())) {
            throw new RuntimeException("Người gửi không thuộc thư viện hiện tại");
        }

        // 3) Verify role cho phép gia hạn (ADMIN/STAFF)
        if (sender.getRole() == null || (sender.getRole() != Role.ADMIN && sender.getRole() != Role.STAFF)) {
            throw new RuntimeException("Bạn không có quyền gia hạn thẻ độc giả");
        }

        // 4) Validate số ngày gia hạn
        if (request.getExtendMonths() == null || request.getExtendMonths() <= 0) {
            throw new RuntimeException("Số ngày gia hạn phải lớn hơn 0");
        }

        Reader reader = readerRepository.findById(pathReaderId)
                                        .orElseThrow(() -> new RuntimeException("Không tìm thấy độc giả"));

        // 5) Reader phải thuộc đúng thư viện
        if (reader.getLibrary() == null || !currentLibraryId.equals(reader.getLibrary().getLibraryId())) {
            throw new RuntimeException("Không thể gia hạn độc giả của thư viện khác");
        }

        // 6) Kiểm tra trạng thái thư viện (nếu SUSPENDED thì chặn)
        Library lib = reader.getLibrary();
        if (lib.getStatus() == StatusLibrary.SUSPENDED) {
            throw new RuntimeException("Thư viện đang bị tạm ngưng, không thể gia hạn");
        }

        // 7) Cập nhật hạn: cộng thêm từ ngày hết hạn hiện tại nếu còn hiệu lực,
        //    nếu đã hết hạn thì cộng từ thời điểm hiện tại
        LocalDateTime base = reader.getMembershipExpiry() != null && reader.getMembershipExpiry().isAfter(LocalDateTime.now())
                ? reader.getMembershipExpiry()
                : LocalDateTime.now();

        reader.setMembershipExpiry(base.plusDays(request.getExtendMonths().longValue()));
        Reader saved = readerRepository.save(reader);

        return mapToReaderResponse(saved);
    }
    private ReaderResponse mapToReaderResponse(Reader reader) {
        return ReaderResponse.builder()
                             .readerId(reader.getReaderId())
                             .fullName(reader.getFullName())
                             .phone(reader.getPhone())
                             .barcode(reader.getBarcode())
                             .isBlocked(reader.getIsBlocked())
                             .createdAt(reader.getCreatedAt())
                             .membershipExpiry(reader.getMembershipExpiry())
                             // .updatedAt(reader.getUpdatedAt()) // (Bổ sung nếu entity có trường này)
                             .build();
    }







}