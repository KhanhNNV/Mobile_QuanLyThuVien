package com.uth.mobileBE.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RenewReaderMembershipRequest {
    private Long senderId;
    private Long readerId;
    private Integer extendMonths; // số ngày gia hạn
}
