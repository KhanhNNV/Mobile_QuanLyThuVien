package com.example.quanlythuvien.ui.category

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.quanlythuvien.R
import com.example.quanlythuvien.core.api.RetrofitClient
import com.example.quanlythuvien.data.model.request.CategoryRequest
import com.example.quanlythuvien.data.remote.CategoryApiService
import com.example.quanlythuvien.data.repository.CategoryRepository
import com.example.quanlythuvien.utils.GenericViewModelFactory
import com.example.quanlythuvien.utils.TokenManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CategoryAddFragment : Fragment(R.layout.fragment_category_add) { // Đổi layout cho đúng của bạn

    private lateinit var edtCategoryName: TextInputEditText
    private lateinit var btnCancelCategory: MaterialButton
    private lateinit var btnSaveCategory: MaterialButton

    private lateinit var viewModel: CategoryAddViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupViewModel()
        observeViewModel()
        setupListeners()
    }

    private fun initViews(view: View) {
        edtCategoryName = view.findViewById(R.id.edtCategoryName)
        btnCancelCategory = view.findViewById(R.id.btnCancelCategory)
        btnSaveCategory = view.findViewById(R.id.btnSaveCategory)
    }

    private fun setupViewModel() {
        val retrofit = RetrofitClient.getInstance(requireContext())
        val apiService = retrofit.create(CategoryApiService::class.java)
        val repository = CategoryRepository(apiService)

        val factory = GenericViewModelFactory { CategoryAddViewModel(repository) }
        viewModel = ViewModelProvider(this, factory)[CategoryAddViewModel::class.java]
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.addCategoryState.collectLatest { state ->
                when (state) {
                    is CategoryAddState.Idle -> {
                        btnSaveCategory.isEnabled = true
                        btnSaveCategory.text = "LƯU THỂ LOẠI"
                    }
                    is CategoryAddState.Loading -> {
                        btnSaveCategory.isEnabled = false
                        btnSaveCategory.text = "Đang xử lý..."
                    }
                    is CategoryAddState.Success -> {
                        btnSaveCategory.isEnabled = true
                        btnSaveCategory.text = "LƯU THỂ LOẠI"
                        Toast.makeText(requireContext(), "Thêm thể loại thành công!", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                    is CategoryAddState.Error -> {
                        btnSaveCategory.isEnabled = true
                        btnSaveCategory.text = "LƯU THỂ LOẠI"
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        btnCancelCategory.setOnClickListener {
            findNavController().popBackStack()
        }

        btnSaveCategory.setOnClickListener {
            val name = edtCategoryName.text.toString().trim()

            if (name.isEmpty()) {
                edtCategoryName.error = "Vui lòng nhập tên thể loại!"
                edtCategoryName.requestFocus()
                return@setOnClickListener
            }

            val libraryId = TokenManager(requireContext()).getLibraryId()
            if (libraryId == null) {
                Toast.makeText(requireContext(), "Lỗi: Không tìm thấy thư viện!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = CategoryRequest(
                name = name,
                libraryId = libraryId
            )

            // Ném data cho ViewModel xử lý
            viewModel.addCategory(request)
        }
    }
}