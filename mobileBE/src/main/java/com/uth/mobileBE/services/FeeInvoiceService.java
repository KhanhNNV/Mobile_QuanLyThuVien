package com.uth.mobileBE.services;

import com.uth.mobileBE.dto.request.FeeInvoiceRequest;
import com.uth.mobileBE.dto.response.FeeInvoiceResponse;
import com.uth.mobileBE.models.FeeInvoice;
import com.uth.mobileBE.models.Library;
import com.uth.mobileBE.models.Loan;
import com.uth.mobileBE.models.Reader;
import com.uth.mobileBE.models.enums.StatusFeeInvoice;
import com.uth.mobileBE.repositories.FeeInvoiceRepository;
import com.uth.mobileBE.repositories.LibraryRepository;
import com.uth.mobileBE.repositories.LoanRepository;
import com.uth.mobileBE.repositories.ReaderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FeeInvoiceService {

    @Autowired
    private FeeInvoiceRepository feeInvoiceRepository;

    @Autowired
    private LibraryRepository libraryRepository;

    @Autowired
    private ReaderRepository readerRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Transactional
    public List<FeeInvoiceResponse> getInvoicesByLibrary(Long libraryId) {
        return feeInvoiceRepository.findByLibrary_LibraryId(libraryId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public FeeInvoiceResponse createFeeInvoice(FeeInvoiceRequest request) {
        // Kiểm tra và lấy các Entity liên quan dựa trên ID
        Library library = libraryRepository.findById(request.getLibraryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Library với id: " + request.getLibraryId()));

        Reader reader = readerRepository.findById(request.getReaderId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Reader với id: " + request.getReaderId()));

        Loan loan = null;
        if (request.getLoanId() != null) {
            loan = loanRepository.findById(request.getLoanId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Loan với id: " + request.getLoanId()));
        }

        LocalDateTime now = LocalDateTime.now();

        FeeInvoice feeInvoice = FeeInvoice.builder()
                .library(library)
                .reader(reader)
                .loan(loan)
                .type(request.getType())
                .totalAmount(request.getTotalAmount())
                .status(request.getStatus())
                .createdAt(now)
                .updateAt(now)
                .build();

        FeeInvoice savedInvoice = feeInvoiceRepository.save(feeInvoice);
        return mapToResponse(savedInvoice);
    }

    public List<FeeInvoiceResponse> getAllFeeInvoices() {
        return feeInvoiceRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public FeeInvoiceResponse getFeeInvoiceById(Long id) {
        FeeInvoice feeInvoice = getFeeInvoiceEntityById(id);
        return mapToResponse(feeInvoice);
    }

    @Transactional
    public FeeInvoiceResponse updateFeeInvoice(Long id, FeeInvoiceRequest request) {
        FeeInvoice existingInvoice = getFeeInvoiceEntityById(id);

        // Cập nhật các trường thông thường
        if (request.getType() != null) {
            existingInvoice.setType(request.getType());
        }
        if (request.getTotalAmount() != null) {
            existingInvoice.setTotalAmount(request.getTotalAmount());
        }
        if (request.getStatus() != null) {
            existingInvoice.setStatus(request.getStatus());
            if (request.getStatus().name().equals("PAID")) {
                Reader reader = existingInvoice.getReader();
                reader.setIsBlocked(false);
                readerRepository.save(reader);
            }
        }

        // Cập nhật các liên kết (Foreign Keys) nếu có gửi lên
        if (request.getLibraryId() != null) {
            Library library = libraryRepository.findById(request.getLibraryId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Library"));
            existingInvoice.setLibrary(library);
        }
        if (request.getReaderId() != null) {
            Reader reader = readerRepository.findById(request.getReaderId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Reader"));
            existingInvoice.setReader(reader);
        }
        if (request.getLoanId() != null) {
            Loan loan = loanRepository.findById(request.getLoanId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Loan"));
            existingInvoice.setLoan(loan);
        }

        existingInvoice.setUpdateAt(LocalDateTime.now());

        FeeInvoice updatedInvoice = feeInvoiceRepository.save(existingInvoice);
        return mapToResponse(updatedInvoice);
    }

    public void deleteFeeInvoice(Long id) {
        FeeInvoice feeInvoice = getFeeInvoiceEntityById(id);
        feeInvoiceRepository.delete(feeInvoice);
    }


    // --- CÁC HÀM HỖ TRỢ DÙNG NỘI BỘ ---

    private FeeInvoice getFeeInvoiceEntityById(Long id) {
        return feeInvoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy FeeInvoice với id: " + id));
    }

    private FeeInvoiceResponse mapToResponse(FeeInvoice invoice) {
        return FeeInvoiceResponse.builder()
                .invoiceId(invoice.getInvoiceId())
                .libraryId(invoice.getLibrary().getLibraryId())
                .readerId(invoice.getReader().getReaderId()) // Giả sử model Reader có getReaderId()
                .readerName(invoice.getReader().getFullName())
                .loanId(invoice.getLoan() != null ? invoice.getLoan().getLoanId() : null) // Giả sử model Loan có getLoanId()
                .type(invoice.getType())
                .totalAmount(invoice.getTotalAmount())
                .status(invoice.getStatus())
                .paymentMethod(invoice.getPaymentMethod())
                .createdAt(invoice.getCreatedAt())
                .updateAt(invoice.getUpdateAt())
                .build();
    }
}