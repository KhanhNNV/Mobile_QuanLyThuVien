package com.uth.mobileBE.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoanStatusSyncEvent {
    private Long loanId;
}