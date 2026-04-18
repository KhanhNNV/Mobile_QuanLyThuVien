package com.uth.mobileBE.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class CreateLoanWithDetailsRequest {
    private Long readerId;
    private List<Long> copyIds;
}
