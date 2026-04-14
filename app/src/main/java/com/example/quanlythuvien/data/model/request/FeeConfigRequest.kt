package com.example.quanlythuvien.data.model.request

import com.example.quanlythuvien.data.model.enums.TypeFeeConfig

data class FeeConfigRequest(
    val feeType: TypeFeeConfig,
    val amount: Double
)