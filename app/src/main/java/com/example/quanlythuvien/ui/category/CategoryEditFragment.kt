package com.example.quanlythuvien.ui.category

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.quanlythuvien.R
import androidx.navigation.fragment.findNavController
import com.example.quanlythuvien.core.api.RetrofitClient
import com.example.quanlythuvien.data.model.request.CategoryRequest
import com.example.quanlythuvien.data.remote.CategoryApiService
import com.example.quanlythuvien.data.repository.CategoryRepository
import com.example.quanlythuvien.utils.GenericViewModelFactory
import com.example.quanlythuvien.utils.TokenManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CategoryEditFragment : Fragment(R.layout.fragment_category_edit) {

    private lateinit var viewModel: CategoryEditViewModel
    private var categoryId: Long = -1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val edtName = view.findViewById<TextInputEditText>(R.id.edtEditCategoryName)
        val btnUpdate = view.findViewById<MaterialButton>(R.id.btnUpdateCategory)
        val btnDelete = view.findViewById<MaterialButton>(R.id.btnDeleteCategory)
        val btnCancel = view.findViewById<MaterialButton>(R.id.btnCancelEdit)
        val role = TokenManager(requireContext()).getRole()
        val parentLayout = btnDelete.parent as android.widget.LinearLayout

        if (role == "STAFF") {

            edtName.isEnabled = false


            btnDelete.visibility = View.GONE
            btnUpdate.visibility = View.GONE

            btnCancel.text = "Quay lại"

            parentLayout.weightSum = 1f

        } else {

            edtName.isEnabled = true
            btnDelete.visibility = View.VISIBLE
            btnUpdate.visibility = View.VISIBLE
            btnCancel.text = "Hủy"
            parentLayout.weightSum = 3f
        }

        // 1. Nhận dữ liệu từ trang Danh sách truyền qua
        categoryId = arguments?.getLong("categoryId") ?: -1
        val categoryName = arguments?.getString("categoryName")
        edtName.setText(categoryName)

        // Setup ViewModel (y chang bên Add)
        val retrofit = RetrofitClient.getInstance(requireContext())
        val apiService = retrofit.create(CategoryApiService::class.java)
        val repository = CategoryRepository(apiService)
        viewModel = ViewModelProvider(
            this,
            GenericViewModelFactory { CategoryEditViewModel(repository) })[CategoryEditViewModel::class.java]

        // 2. Xử lý nút Cập nhật
        btnUpdate.setOnClickListener {
            val newName = edtName.text.toString().trim()
            if (newName.isEmpty()) return@setOnClickListener

            val libraryId = TokenManager(requireContext()).getLibraryId() ?: return@setOnClickListener
            viewModel.updateCategory(categoryId, CategoryRequest(newName, libraryId))
        }

        // 3. Xử lý nút Xóa (Hiện Dialog xác nhận cho chắc)
        btnDelete.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xóa thể loại")
                .setMessage("Bạn có chắc chắn muốn xóa thể loại này không?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa") { _, _ ->
                    viewModel.deleteCategory(categoryId)
                }
                .show()
        }

        btnCancel.setOnClickListener { findNavController().popBackStack() }

        // 4. Lắng nghe trạng thái
        observeViewModel()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.editState.collectLatest { state ->
                when (state) {
                    is CategoryEditState.Loading -> { /* Hiện loading */ }
                    is CategoryEditState.UpdateSuccess -> {
                        Toast.makeText(requireContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                    is CategoryEditState.DeleteSuccess -> {
                        Toast.makeText(requireContext(), "Đã xóa thể loại", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                    is CategoryEditState.Error -> {
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }
}