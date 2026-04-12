package com.example.quanlythuvien.ui.welcome.category


import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.quanlythuvien.R
import com.example.quanlythuvien.core.api.RetrofitClient
import com.example.quanlythuvien.data.remote.CategoryApiService
import com.example.quanlythuvien.data.repository.CategoryRepository
import com.example.quanlythuvien.utils.GenericViewModelFactory
import com.example.quanlythuvien.utils.JwtUtils
import com.example.quanlythuvien.utils.TokenManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CreateCategoryFragment : Fragment(R.layout.fragment_create_category) {

    private lateinit var etCategoryName: EditText
    private lateinit var btnNextToBook: Button
    private lateinit var viewModel: CreateCategoryViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupViewModel()
        observeViewModel()
        setupListeners()
    }

    private fun initViews(view: View) {
        etCategoryName = view.findViewById(R.id.etCategoryName)
        btnNextToBook = view.findViewById(R.id.btnNextToBook)
    }

    private fun setupViewModel() {
        val apiService = RetrofitClient.getInstance(requireContext()).create(CategoryApiService::class.java)
        val repository = CategoryRepository(apiService)
        val factory = GenericViewModelFactory{ CreateCategoryViewModel(repository) }
        viewModel = ViewModelProvider(this, factory)[CreateCategoryViewModel::class.java]
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collectLatest { state ->
                when (state) {
                    is CategoryState.Idle -> {
                        btnNextToBook.isEnabled = true
                        btnNextToBook.text = getString(R.string.txt_buttonNextCate)
                    }
                    is CategoryState.Loading -> {
                        btnNextToBook.isEnabled = false
                        btnNextToBook.text = "Đang tạo..."
                    }
                    is CategoryState.Success -> {
                        Toast.makeText(requireContext(), "Tạo danh mục thành công!", Toast.LENGTH_SHORT).show()
                        val bundle = Bundle().apply {
                            putLong("categoryId", state.categoryId)
                        }
                        findNavController().navigate(R.id.createBookFragment,bundle)
                    }
                    is CategoryState.Error -> {
                        btnNextToBook.isEnabled = true
                        btnNextToBook.text = getString(R.string.txt_buttonNextCate)
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        btnNextToBook.setOnClickListener {
            val categoryName = etCategoryName.text.toString().trim()
            if (categoryName.isEmpty()) {
                etCategoryName.error = "Vui lòng nhập tên danh mục"
                return@setOnClickListener
            }

            val libraryId = TokenManager(requireContext()).getLibraryId()

            if (libraryId != null) {
                viewModel.createCategory(categoryName, libraryId)
            } else {
                Toast.makeText(requireContext(), "Lỗi: Không tìm thấy thư viện hoặc chưa đăng nhập!", Toast.LENGTH_SHORT).show()
            }
        }
    }

}