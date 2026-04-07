package com.example.quanlythuvien.ui.borrow_pay

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.navigation.fragment.findNavController
import com.example.quanlythuvien.R
import com.example.quanlythuvien.utils.setupHeaderWithBack

class LoanPolicyFragment : Fragment(R.layout.fragment_loan_policy) {
    private lateinit var btnBack: ImageButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupHeaderWithBack(view, "Quản lý chính sách mượn trả")

        initViews(view)
        handleButtonBackEvent()
    }

    private fun initViews(view: View){
        btnBack=view.findViewById(R.id.btnBack)
    }

    private fun handleButtonBackEvent(){
        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

}