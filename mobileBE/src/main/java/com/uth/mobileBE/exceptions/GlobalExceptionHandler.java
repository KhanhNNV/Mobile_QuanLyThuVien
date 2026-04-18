package com.uth.mobileBE.exceptions;

import jakarta.validation.ConstraintViolationException;
import org.apache.coyote.BadRequestException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /* ===================== 400 - @Valid RequestBody ===================== */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        400,
                        "Dữ liệu không hợp lệ",
                        errors,
                        LocalDateTime.now()
                ));
    }

    /* ===================== 400 - @Validated Param ===================== */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        v -> v.getPropertyPath().toString(),
                        v -> v.getMessage(),
                        (msg1, msg2) -> msg1
                ));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        400,
                        "Tham số không hợp lệ",
                        errors,
                        LocalDateTime.now()
                ));
    }

    /* ===================== 400 - Business ===================== */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        400,
                        "Yêu cầu không hợp lệ",
                        ex.getMessage(),
                        LocalDateTime.now()
                ));
    }

    /* ===================== 400 - JSON sai format ===================== */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleInvalidJson(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        400,
                        "Dữ liệu không hợp lệ",
                        "JSON không đúng định dạng hoặc sai kiểu dữ liệu",
                        LocalDateTime.now()
                ));
    }

    /* ===================== 400 - Thiếu param ===================== */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        400,
                        "Thiếu tham số",
                        "Thiếu tham số bắt buộc: " + ex.getParameterName(),
                        LocalDateTime.now()
                ));
    }

    /* ===================== 400 - Sai kiểu dữ liệu ===================== */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format(
                "Giá trị '%s' không hợp lệ cho tham số '%s'. Yêu cầu kiểu: %s",
                ex.getValue(),
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "không xác định"
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        400,
                        "Sai kiểu dữ liệu",
                        message,
                        LocalDateTime.now()
                ));
    }

    /* ===================== 401 ===================== */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(
                        401,
                        "Chưa xác thực",
                        "Vui lòng đăng nhập để tiếp tục",
                        LocalDateTime.now()
                ));
    }

    /* ===================== 403 ===================== */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(
                        403,
                        "Không có quyền truy cập",
                        "Bạn không có quyền thực hiện chức năng này",
                        LocalDateTime.now()
                ));
    }

    /* ===================== 404 ===================== */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(
                        404,
                        "Không tìm thấy dữ liệu",
                        ex.getMessage(),
                        LocalDateTime.now()
                ));
    }

    /* ===================== 409 ===================== */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DataIntegrityViolationException ex) {
        String message = "Dữ liệu đã tồn tại hoặc vi phạm ràng buộc";
        if (ex.getMessage().contains("Duplicate entry")) {
            message = "Dữ liệu đã tồn tại trong hệ thống";
        }

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(
                        409,
                        "Xung đột dữ liệu",
                        message,
                        LocalDateTime.now()
                ));
    }

    /* ===================== 400 - Độc giả có vi phạm chưa xử lý ===================== */
    @ExceptionHandler(ReaderHasActiveViolationsException.class)
    public ResponseEntity<ErrorResponse> handleActiveViolations(ReaderHasActiveViolationsException ex) {
        Map<String, Object> details = new HashMap<>();
        details.put("message", ex.getMessage());
        details.put("violations", ex.getActiveViolations());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        400,
                        "Không thể mượn sách",
                        details,
                        LocalDateTime.now()
                ));
    }

    /* ===================== 400 - RuntimeException ===================== */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        400,
                        "Lỗi xử lý",
                        ex.getMessage(),
                        LocalDateTime.now()
                ));
    }

    /* ===================== 500 ===================== */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        // Log lỗi chi tiết để debug
        ex.printStackTrace();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                        500,
                        "Lỗi hệ thống",
                        "Đã xảy ra lỗi không xác định. Vui lòng thử lại sau.",
                        LocalDateTime.now()
                ));
    }
}