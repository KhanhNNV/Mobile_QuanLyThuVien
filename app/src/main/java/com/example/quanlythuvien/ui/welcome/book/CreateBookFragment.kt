package com.example.quanlythuvien.ui.welcome.book

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.quanlythuvien.R
import com.example.quanlythuvien.core.api.RetrofitClient
import com.example.quanlythuvien.data.model.request.InitialBookRequest
import com.example.quanlythuvien.data.remote.BookApiService
import com.example.quanlythuvien.data.repository.BookRepository
import com.example.quanlythuvien.utils.GenericViewModelFactory
import com.example.quanlythuvien.utils.JwtUtils
import com.example.quanlythuvien.utils.TokenManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CreateBookFragment : Fragment(R.layout.fragment_create_book) {

    // Đã sửa lại tên biến cho khớp ID với XML
    private lateinit var etBookName: EditText
    private lateinit var etBookAuthor: EditText
    private lateinit var etBookCode: EditText
    private lateinit var etBookBasePrice: EditText
    private lateinit var btnFinishSetup: Button

    private lateinit var viewModel: CreateBookViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        initViews(view)
        setupViewModel()
        observeViewModel()
        setupListeners()
    }

    private fun initViews(view: View) {
        // Ánh xạ View chuẩn với XML
        etBookName = view.findViewById(R.id.etBookName)
        etBookAuthor = view.findViewById(R.id.etBookAuthor)
        etBookCode = view.findViewById(R.id.etBookCode)
        etBookBasePrice = view.findViewById(R.id.etBookBasePrice)
        btnFinishSetup = view.findViewById(R.id.btnFinishSetup)
    }

    private fun setupViewModel() {
        val apiService = RetrofitClient.getInstance(requireContext()).create(BookApiService::class.java)
        val repository = BookRepository(apiService)
        val factory = GenericViewModelFactory{ CreateBookViewModel(repository) }
        viewModel = ViewModelProvider(this, factory)[CreateBookViewModel::class.java]
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collectLatest { state ->
                when (state) {
                    is BookState.Idle -> {
                        btnFinishSetup.isEnabled = true
                        btnFinishSetup.text = getString(R.string.txt_buttonCompletedAddBook)
                    }
                    is BookState.Loading -> {
                        btnFinishSetup.isEnabled = false
                        btnFinishSetup.text = "Đang xử lý..."
                    }
                    is BookState.Success -> {
                        Toast.makeText(requireContext(), "Thiết lập thành công! Chào mừng bạn.", Toast.LENGTH_LONG).show()
                        navigateToDashboard()
                    }
                    is BookState.Error -> {
                        btnFinishSetup.isEnabled = true
                        btnFinishSetup.text = getString(R.string.txt_buttonCompletedAddBook)
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        btnFinishSetup.setOnClickListener {
            val title = etBookName.text.toString().trim()
            val author = etBookAuthor.text.toString().trim()
            val barcode = etBookCode.text.toString().trim() // Dùng mã sách làm barcode
            val priceStr = etBookBasePrice.text.toString().trim()

            if (title.isEmpty() || author.isEmpty() || barcode.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập đủ thông tin bắt buộc!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val basePrice = priceStr.toDoubleOrNull() ?: 0.0

            // Lấy libraryId
            val libraryId = TokenManager(requireContext()).getLibraryId()
            if (libraryId == null) {
                Toast.makeText(requireContext(), "Lỗi: Không tìm thấy thông tin thư viện!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val categoryId = arguments?.getLong("categoryId", -1L) ?: -1L

            if (categoryId == -1L) {

                Toast.makeText(requireContext(), "Lỗi: Không lấy được thông tin Danh mục! ID = $categoryId", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val request = InitialBookRequest(
                title = title,
                author = author,
                isbn = "Temp",
                basePrice = basePrice,
                categoryId = categoryId,
                libraryId = libraryId,
                barcode = barcode
            )

            viewModel.createInitialBook(request)
        }
    }

    private fun navigateToDashboard() {
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.nav_graph, true)
            .build()
        findNavController().navigate(R.id.dashboardFragment, null, navOptions)
    }
}