package com.uth.mobileBE.services;

import com.uth.mobileBE.Utils.SecurityUtils;
import com.uth.mobileBE.dto.request.ExtendMembershipExpiryRequest;
import com.uth.mobileBE.dto.request.ReaderRequest;
import com.uth.mobileBE.dto.response.ReaderResponse;
import com.uth.mobileBE.models.FeeConfig;
import com.uth.mobileBE.models.FeeInvoice;
import com.uth.mobileBE.models.Library;
import com.uth.mobileBE.models.Reader;
import com.uth.mobileBE.models.enums.StatusFeeInvoice;
import com.uth.mobileBE.models.enums.TypeFeeConfig;
import com.uth.mobileBE.models.enums.TypeFeeInvoice;
import com.uth.mobileBE.repositories.FeeConfigRepository;
import com.uth.mobileBE.repositories.FeeInvoiceRepository;
import com.uth.mobileBE.repositories.LibraryRepository;
import com.uth.mobileBE.repositories.ReaderRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReaderService {
    private final ReaderRepository readerRepository;
    private final LibraryRepository libraryRepository;
    private final FeeInvoiceRepository feeInvoiceRepository;
    private final FeeConfigRepository feeConfigRepository;

    //Tạo người độc giả
    @Transactional
    public ReaderResponse createReader(ReaderRequest request) {
        Long currentLibraryId = SecurityUtils.getLibraryId();
        Library library = libraryRepository.findById(currentLibraryId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thư viện"));


        // Lấy số ID to nhất trong hệ thống hiện tại
        Long maxId = readerRepository.findMaxReaderId();

        // Tạo mã mới bằng cách lấy ID to nhất + 1 (Ví dụ đang có người DG-5 thì tạo DG-6)
        String generatedBarcode = "READER-" + (maxId + 1);
        // ----------------------------------------------

        LocalDateTime membershipExpiry = calculateExpiryDate(request.getMonthRegis());

        Reader reader = Reader.builder()
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .barcode(generatedBarcode)
                .membershipExpiry(membershipExpiry)
                .library(library)
                .isBlocked(true)
                .createdAt(LocalDateTime.now())
                .build();

        Reader saved = readerRepository.save(reader);

        FeeConfig feeRegistration= feeConfigRepository.findByLibrary_LibraryIdAndFeeType(library.getLibraryId(), TypeFeeConfig.REG_NORMAL)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phí đăng ký thẻ cho thư viện này"));

        Double totalAmount = feeRegistration.getAmount() * request.getMonthRegis();


        FeeInvoice feeInvoice = FeeInvoice.builder()
                .reader(reader)
                .library(library)
                .type(TypeFeeInvoice.REGISTRATION)
                .totalAmount(totalAmount)
                .status(StatusFeeInvoice.UNPAID)
                .build();

        feeInvoiceRepository.save(feeInvoice);
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

    @Transactional
    public ReaderResponse updateReader(Long id, ReaderRequest request) {
        Reader reader = readerRepository.findById(id)
                                        .orElseThrow(() -> new RuntimeException("Không tìm thấy độc giả để cập nhật"));

        // Cập nhật các trường thông tin
        reader.setFullName(request.getFullName());
        reader.setPhone(request.getPhone());
        reader.setBarcode(request.getBarcode());
        reader.setMembershipExpiry(calculateExpiryDate(request.getMonthRegis()));

        // Nếu muốn cho phép chuyển thư viện, bạn có thể xử lý libraryId ở đây
        if (request.getLibraryId() != null && !request.getLibraryId().equals(reader.getLibrary().getLibraryId())) {
            Library library = libraryRepository.findById(request.getLibraryId())
                                               .orElseThrow(() -> new RuntimeException("Không tìm thấy thư viện"));
            reader.setLibrary(library);
        }

        Reader updated = readerRepository.save(reader);
        return mapToReaderResponse(updated);
    }

    @Transactional
    public void deleteReader(Long id) {
        if (!readerRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy độc giả để xóa");
        }
        readerRepository.deleteById(id);
    }

    public Long countReaders(Long libraryId) {
        return readerRepository.countByLibrary_LibraryId(libraryId);
    }

    private LocalDateTime calculateExpiryDate(Long months){
        return LocalDateTime.now().plusMonths(months);
    }

    public ReaderResponse extendMembershipExpiry(Long id, ExtendMembershipExpiryRequest request) {
        Long currentLibraryId = SecurityUtils.getLibraryId();
        Library library = libraryRepository.findById(currentLibraryId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thư viện"));

        Reader reader=readerRepository.findById(id).orElseThrow(()-> new RuntimeException("Không tìm thấy độc giả này"));
        LocalDateTime membershipExpiryNew = reader.getMembershipExpiry().plusMonths(request.getMonthRegis());
        reader.setMembershipExpiry(membershipExpiryNew);
        Reader saved = readerRepository.save(reader);

        FeeConfig feeRegistration= feeConfigRepository.findByLibrary_LibraryIdAndFeeType(library.getLibraryId(), TypeFeeConfig.REG_NORMAL)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phí đăng ký thẻ cho thư viện này"));

        Double totalAmount = feeRegistration.getAmount() * request.getMonthRegis();


        FeeInvoice feeInvoice = FeeInvoice.builder()
                .reader(reader)
                .library(library)
                .type(TypeFeeInvoice.RENEWAL)
                .totalAmount(totalAmount)
                .status(StatusFeeInvoice.UNPAID)
                .build();

        feeInvoiceRepository.save(feeInvoice);

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
                             .updatedAt(LocalDateTime.now())
                             .build();
    }



}