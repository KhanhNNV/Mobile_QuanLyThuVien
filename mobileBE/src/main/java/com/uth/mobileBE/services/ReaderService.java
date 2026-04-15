package com.uth.mobileBE.services;

import com.uth.mobileBE.dto.request.ReaderRequest;
import com.uth.mobileBE.dto.response.ReaderResponse;
import com.uth.mobileBE.models.Library;
import com.uth.mobileBE.models.Reader;
import com.uth.mobileBE.repositories.LibraryRepository;
import com.uth.mobileBE.repositories.ReaderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReaderService {
    private final ReaderRepository readerRepository;
    private final LibraryRepository libraryRepository;

    @Transactional
    public ReaderResponse createReader(ReaderRequest request) {
        Library library = libraryRepository.findById(request.getLibraryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thư viện"));


        // Lấy số ID to nhất trong hệ thống hiện tại
        Long maxId = readerRepository.findMaxReaderId();

        // Tạo mã mới bằng cách lấy ID to nhất + 1 (Ví dụ đang có người DG-5 thì tạo DG-6)
        String generatedBarcode = "READER-" + (maxId + 1);
        // ----------------------------------------------

        Reader reader = Reader.builder()
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .barcode(generatedBarcode)
                .membershipExpiry(request.getMembershipExpiry())
                .library(library)
                .isBlocked(false) // Mặc định không bị khóa
                .createdAt(LocalDateTime.now())
                .build();

        Reader saved = readerRepository.save(reader);
        return mapToReaderResponse(saved);
    }

    // --- BỔ SUNG CRUD ---

    public List<ReaderResponse> getAllReaders() {
        return readerRepository.findAll().stream()
                               .map(this::mapToReaderResponse)
                               .collect(Collectors.toList());
    }

    public ReaderResponse getReaderById(Long id) {
        Reader reader = readerRepository.findById(id)
                                        .orElseThrow(() -> new RuntimeException("Không tìm thấy độc giả"));
        return mapToReaderResponse(reader);
    }

    @Transactional
    public ReaderResponse updateReader(Long id, ReaderRequest request) {
        Reader reader = readerRepository.findById(id)
                                        .orElseThrow(() -> new RuntimeException("Không tìm thấy độc giả để cập nhật"));

        // Cập nhật các trường thông tin
        reader.setFullName(request.getFullName());
        reader.setPhone(request.getPhone());
        reader.setBarcode(request.getBarcode());
        reader.setMembershipExpiry(request.getMembershipExpiry());

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