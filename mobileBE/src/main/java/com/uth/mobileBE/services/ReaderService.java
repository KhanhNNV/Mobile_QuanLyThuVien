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

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReaderService {
    private final ReaderRepository readerRepository;
    private final LibraryRepository libraryRepository;

    //Tạo người độc giả
    @Transactional
    public ReaderResponse createReader(ReaderRequest request) {
        Long currentLibraryId = SecurityUtils.getCurrentUserLibraryId();
        Reader reader = Reader.builder()
                              .fullName(request.getFullName())
                              .phone(request.getPhone())
                              .barcode(request.getBarcode())
                              .isStudent(request.getIsStudent())
                              .membershipExpiry(request.getMembershipExpiry())
                              .library(library)
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
     * @param page Số thứ tự trang (bắt đầu từ 0)
     * @param size Số lượng phần tử trên 1 trang (ví dụ: 10)
     */
    public List<ReaderResponse> getReadersPaginated(Long libraryId) {

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
        if (request == null || request.isEmpty()) { return null; }
        List<Reader> listReader = readerRepository.searchReaders(request.trim());
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
        reader.setIsStudent(request.getIsStudent());
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
                             .isStudent(reader.getIsStudent())
                             .isBlocked(reader.getIsBlocked())
                             .createdAt(reader.getCreatedAt())
                             .membershipExpiry(reader.getMembershipExpiry())
                             // .updatedAt(reader.getUpdatedAt()) // (Bổ sung nếu entity có trường này)
                             .build();
    }




}