package com.example.quanlythuvien.ui.borrow_pay

import android.app.AlertDialog
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

    private val dummyData = mutableListOf(
        LoanPolicy("P01", "Sách Giáo Khoa", 30, "Học sinh"),
        LoanPolicy("P02", "Tiểu Thuyết", 7, "Học sinh"),
        LoanPolicy("P03", "Truyện Tranh", 3, "Thường"),
        LoanPolicy("P04", "Tài Liệu Chuyên Ngành", 14, "Thường")
    )

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

        // Khởi tạo Adapter với callback khi bấm nút Sửa (Cây bút)
        policyAdapter = LoanPolicyAdapter(
            policyList = dummyData,
            onEditClick = { selectedPolicy ->
                showPolicyDialog(selectedPolicy)
            },
            onDeleteClick = { selectedPolicy, position ->
                showDeleteConfirmDialog(selectedPolicy, position)
            }
        )
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
    private fun showDeleteConfirmDialog(policy: LoanPolicy, position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Xóa chính sách")
            .setMessage("Bạn có chắc chắn muốn xóa chính sách cho loại sách này không?")
            .setPositiveButton("Xóa") { dialog, _ ->
                // Xóa khỏi danh sách
                dummyData.removeAt(position)
                // Thông báo cho adapter biết item ở vị trí này đã bị xóa để update giao diện mượt mà
                policyAdapter.notifyItemRemoved(position)
                // Cập nhật lại vị trí của các item bên dưới (tránh lỗi crash khi xóa tiếp)
                policyAdapter.notifyItemRangeChanged(position, dummyData.size)

                Toast.makeText(requireContext(), "Đã xóa chính sách", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Hủy") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

}