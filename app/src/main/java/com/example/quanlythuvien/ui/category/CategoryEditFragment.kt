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

class CategoryEditFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Nạp giao diện XML của màn hình Thêm Thể Loại
        return inflater.inflate(R.layout.fragment_category_edit, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnCancelEdit = view.findViewById<MaterialButton>(R.id.btnCancelEdit)
        val edtEditCategoryName = view.findViewById<TextInputEditText>(R.id.edtEditCategoryName)
        val btnUpdateCategory = view.findViewById<MaterialButton>(R.id.btnUpdateCategory)
        val btnDeleteCategory = view.findViewById<MaterialButton>(R.id.btnDeleteCategory)
        btnCancelEdit.setOnClickListener {
            findNavController().popBackStack()
        }
        btnDeleteCategory.setOnClickListener {
            Toast.makeText(requireContext(), "Đã xóa thể loại!", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
        btnUpdateCategory.setOnClickListener {
            val categoryName = edtEditCategoryName.text.toString().trim()
            if (categoryName.isEmpty())
            {
                edtEditCategoryName.error = "Vui lòng nhập tên thể loại !"
                edtEditCategoryName.requestFocus()
            }else{
                Toast.makeText(requireContext(), "Đã cập nhật: $categoryName", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }

    }


}