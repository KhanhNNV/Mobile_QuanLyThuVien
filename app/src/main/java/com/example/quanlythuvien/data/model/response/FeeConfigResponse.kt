package com.example.quanlythuvien.data.model.response

import com.example.quanlythuvien.data.model.enums.TypeFeeConfig

data class FeeConfigResponse(
    val configId: Long,
    val feeType: TypeFeeConfig,
    val amount: Double
)