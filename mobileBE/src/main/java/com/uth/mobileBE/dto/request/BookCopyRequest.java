package com.uth.mobileBE.dto.request;

import com.uth.mobileBE.models.enums.ConditionBookCopy;
import com.uth.mobileBE.models.enums.StatusBookCopy;
import lombok.Data;

@Data
public class BookCopyRequest {
    private Long bookId;
    private String barcode;
    private ConditionBookCopy condition;
    private StatusBookCopy status;
}
