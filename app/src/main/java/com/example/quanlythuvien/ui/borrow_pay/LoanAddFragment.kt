package com.example.quanlythuvien.ui.borrow_pay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.quanlythuvien.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class LoanAddFragment : Fragment() {
    private lateinit var containerBooks: LinearLayout
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_loan_add, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnCancelLoan = view.findViewById<MaterialButton>(R.id.btnCancelLoan)
        val btnSaveLoan = view.findViewById<MaterialButton>(R.id.btnSaveLoan)
        val edtLoanReaderId = view.findViewById<TextInputEditText>(R.id.edtLoanReaderId)
        btnCancelLoan.setOnClickListener {
            findNavController().popBackStack()
        }
        btnSaveLoan.setOnClickListener {
            val readerId = edtLoanReaderId.text.toString().trim()

            // Kiểm tra Mã độc giả
            if (readerId.isEmpty()) {
                edtLoanReaderId.error = "Vui lòng nhập Mã độc giả!"
                edtLoanReaderId.requestFocus()
                return@setOnClickListener
            }
        }
    }
}



