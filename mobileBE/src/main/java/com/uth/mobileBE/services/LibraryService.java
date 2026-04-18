package com.uth.mobileBE.services;

import com.uth.mobileBE.Utils.SecurityUtils;
import com.uth.mobileBE.dto.request.LibraryRequest;
import com.uth.mobileBE.dto.response.LibraryResponse;
import com.uth.mobileBE.models.Library;
import com.uth.mobileBE.models.enums.StatusLibrary;
import com.uth.mobileBE.repositories.LibraryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LibraryService {

    @Autowired
    private LibraryRepository libraryRepository;

    public LibraryResponse createLibrary(LibraryRequest request) {
        LocalDateTime now = LocalDateTime.now();

        Library library = Library.builder()
                .name(request.getName())
                .address(request.getAddress())
                .status(request.getStatus() != null ? request.getStatus() : StatusLibrary.ACTIVE)
                .maxLoansQuota(0)
                .maxBooksQuota(0)
                .createdAt(now)
                .updateAt(now)
                .build();

        Library savedLibrary = libraryRepository.save(library);
        return mapToResponse(savedLibrary);
    }

    public List<LibraryResponse> getAllLibraries() {
        return libraryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public LibraryResponse getLibraryById() {
        Long id = SecurityUtils.getLibraryId();
        Library library = getLibraryEntityById(id);
        return mapToResponse(library);
    }

    public LibraryResponse updateLibrary(LibraryRequest request) {
        Long id = SecurityUtils.getLibraryId();
        Library existingLibrary = getLibraryEntityById(id);

        if (request.getName() != null) {
            existingLibrary.setName(request.getName());
        }
        if (request.getAddress() != null) {
            existingLibrary.setAddress(request.getAddress());
        }
        if (request.getStatus() != null) {
            existingLibrary.setStatus(request.getStatus());
        }

        existingLibrary.setUpdateAt(LocalDateTime.now());

        Library updatedLibrary = libraryRepository.save(existingLibrary);
        return mapToResponse(updatedLibrary);
    }

    public void deleteLibrary(Long id) {
        Library library = getLibraryEntityById(id);
        libraryRepository.delete(library);
    }


    public LibraryResponse updateMaxLoansQuota(Integer newQuota) {
        Long id = SecurityUtils.getLibraryId();
        Library existingLibrary = getLibraryEntityById(id);

        if (newQuota == null || newQuota < 0) {
            throw new IllegalArgumentException("Hạn ngạch không hợp lệ!");
        }

        existingLibrary.setMaxLoansQuota(newQuota);
        existingLibrary.setUpdateAt(LocalDateTime.now());

        Library updatedLibrary = libraryRepository.save(existingLibrary);
        return mapToResponse(updatedLibrary);
    }

    public LibraryResponse updateMaxBooksQuota(Integer newQuota) {
        Long id = SecurityUtils.getLibraryId();
        Library existingLibrary = getLibraryEntityById(id);

        if (newQuota == null || newQuota < 0) {
            throw new IllegalArgumentException("Hạn ngạch không hợp lệ!");
        }

        existingLibrary.setMaxBooksQuota(newQuota);
        existingLibrary.setUpdateAt(LocalDateTime.now());

        Library updatedLibrary = libraryRepository.save(existingLibrary);
        return mapToResponse(updatedLibrary);
    }

    // --- CÁC HÀM HỖ TRỢ DÙNG NỘI BỘ ---

    private Library getLibraryEntityById(Long id) {
        return libraryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Library với id: " + id));
    }

    private LibraryResponse mapToResponse(Library library) {
        return LibraryResponse.builder()
                .libraryId(library.getLibraryId())
                .name(library.getName())
                .address(library.getAddress())
                .status(library.getStatus())
                .maxLoansQuota(library.getMaxLoansQuota())
                .maxBooksQuota(library.getMaxBooksQuota())
                .createdAt(library.getCreatedAt())
                .updateAt(library.getUpdateAt())
                .build();
    }
}
