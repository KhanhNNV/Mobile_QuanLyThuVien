package com.example.quanlythuvien.ui.category

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.quanlythuvien.R
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
class CategoryAddFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Nạp giao diện XML của màn hình Thêm Thể Loại
        return inflater.inflate(R.layout.fragment_category_add, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val btnCancelCategory = view.findViewById<MaterialButton>(R.id.btnCancelCategory)
        val btnSaveCategory = view.findViewById<MaterialButton>(R.id.btnSaveCategory)
        val edtCategoryName = view.findViewById<TextInputEditText>(R.id.edtCategoryName)


        btnCancelCategory.setOnClickListener {

            findNavController().popBackStack()
        }
        btnSaveCategory.setOnClickListener {
            val categoryName = edtCategoryName.text.toString().trim()
            if (categoryName.isEmpty()) {

                edtCategoryName.error = "Vui lòng nhập tên thể loại!"
                edtCategoryName.requestFocus() // Đẩy con trỏ chuột nhấp nháy lại đúng ô đó
            } else {
                Toast.makeText(requireContext(), "Đã lưu thể loại: $categoryName", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
    }
}