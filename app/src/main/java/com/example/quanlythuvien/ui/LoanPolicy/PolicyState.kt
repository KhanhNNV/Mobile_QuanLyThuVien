package com.example.quanlythuvien.ui.LoanPolicy

import com.example.quanlythuvien.data.model.response.LoanPolicyResponse

sealed class PolicyState {
    object Idle : PolicyState()
    object Loading : PolicyState()
    data class SuccessList(val policies: List<LoanPolicyResponse>) : PolicyState()
    data class SuccessAction(val message: String) : PolicyState()
    data class Error(val message: String) : PolicyState()
}