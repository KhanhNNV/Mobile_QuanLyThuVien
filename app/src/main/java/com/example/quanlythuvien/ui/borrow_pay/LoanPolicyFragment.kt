package com.example.quanlythuvien.ui.borrow_pay

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R
import com.example.quanlythuvien.ui.borrow_pay.adapter.LoanPolicyAdapter
import com.example.quanlythuvien.ui.borrow_pay.data.LoanPolicy
import com.example.quanlythuvien.utils.setupHeaderWithBack
import com.google.android.material.floatingactionbutton.FloatingActionButton

class LoanPolicyFragment : Fragment(R.layout.fragment_loan_policy) {
    private lateinit var btnBack: ImageButton
    private lateinit var fabAddPolicy: FloatingActionButton
    private lateinit var rvPolicies: RecyclerView
    private lateinit var policyAdapter: LoanPolicyAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupHeaderWithBack(view, "Quản lý chính sách mượn trả")

        initViews(view)
        setupRecyclerView()
        handleButtonBackEvent()
        handleFabAddPolicyEvent()
    }

    private fun initViews(view: View){
        btnBack=view.findViewById(R.id.btnBack)
        fabAddPolicy=view.findViewById(R.id.fabAddPolicy)
        rvPolicies = view.findViewById(R.id.rvPolicies)
    }
    private fun setupRecyclerView() {
        rvPolicies.layoutManager = LinearLayoutManager(requireContext())

        // Dữ liệu mẫu (Sample Data)
        val dummyData = mutableListOf(
            LoanPolicy("P01", "Sách Giáo Khoa", 30, "Học sinh"),
            LoanPolicy("P02", "Tiểu Thuyết", 7, "Học sinh"),
            LoanPolicy("P03", "Truyện Tranh", 3, "Thường"),
            LoanPolicy("P04", "Tài Liệu Chuyên Ngành", 14, "Thường")
        )

        // Khởi tạo Adapter với callback khi bấm nút Sửa (Cây bút)
        policyAdapter = LoanPolicyAdapter(dummyData) { selectedPolicy ->
            // Mở hộp thoại và truyền dữ liệu cũ vào để Sửa
            showPolicyDialog(selectedPolicy)
        }
        rvPolicies.adapter = policyAdapter
    }

    private fun handleButtonBackEvent(){
        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun handleFabAddPolicyEvent() {
        fabAddPolicy.setOnClickListener {
            showPolicyDialog()
        }
    }


    private fun showPolicyDialog(policyToEdit: LoanPolicy? = null) {
        val dialog = android.app.Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_loan_policy)

        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
        dialog.window?.setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val tvDialogTitle = dialog.findViewById<TextView>(R.id.tvDialogTitle)
        val spinnerCategory = dialog.findViewById<Spinner>(R.id.spinnerCategory)
        val edtMaxDays = dialog.findViewById<EditText>(R.id.edtMaxDays)
        val rgCustomerType = dialog.findViewById<RadioGroup>(R.id.rgCustomerType)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
        val btnSavePolicy = dialog.findViewById<Button>(R.id.btnSavePolicy)

        // Nếu là chế độ SỬA, đổ dữ liệu cũ lên giao diện
        if (policyToEdit != null) {
            tvDialogTitle?.text = "Sửa Chính Sách"
            edtMaxDays?.setText(policyToEdit.maxDays.toString())

            // Set lại RadioButton theo đối tượng cũ
            when (policyToEdit.targetCustomer) {
                "Học sinh" -> rgCustomerType?.check(R.id.rbStudent)
                "Thường" -> rgCustomerType?.check(R.id.rbRegular)
                else -> rgCustomerType?.check(R.id.rbAll)
            }
            // (TODO: Xử lý chọn lại giá trị cho Spinner thể loại sách nếu cần)
        } else {
            tvDialogTitle?.text = "Thêm Chính Sách"
        }

        btnCancel?.setOnClickListener {
            dialog.dismiss()
        }

        btnSavePolicy?.setOnClickListener {
            val maxDaysStr = edtMaxDays?.text.toString().trim()

            if (maxDaysStr.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập số ngày mượn tối đa!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val targetCustomer = when (rgCustomerType?.checkedRadioButtonId) {
                R.id.rbStudent -> "Học sinh"
                R.id.rbRegular -> "Thường"
                else -> "Tất cả"
            }

            // Xử lý logic lưu dữ liệu ở đây (Gọi ViewModel / API)
            if (policyToEdit != null) {
                Toast.makeText(requireContext(), "Đã CẬP NHẬT: $targetCustomer - $maxDaysStr ngày", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Đã THÊM MỚI: $targetCustomer - $maxDaysStr ngày", Toast.LENGTH_SHORT).show()
            }

            dialog.dismiss()
        }

        dialog.show()
    }

}