package com.example.quanlythuvien.ui.books.addBook

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.quanlythuvien.R
import com.example.quanlythuvien.core.api.RetrofitClient
import com.example.quanlythuvien.data.model.request.BookRequest
import com.example.quanlythuvien.data.model.response.CategoryResponse
import com.example.quanlythuvien.data.remote.BookApiService
import com.example.quanlythuvien.data.remote.CategoryApiService
import com.example.quanlythuvien.data.repository.BookRepository
import com.example.quanlythuvien.data.repository.CategoryRepository
import com.example.quanlythuvien.ui.books.addBook.AddBookState
import com.example.quanlythuvien.ui.books.addBook.AddBookViewModel
import com.example.quanlythuvien.utils.BookWarehousePermissions
import com.example.quanlythuvien.utils.GenericViewModelFactory
import com.example.quanlythuvien.utils.TokenManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AddBookFragment : Fragment(R.layout.fragment_add_book) {

    // 1. Khai báo các View (Giống sếp)
    private lateinit var btnCancel: MaterialButton
    private lateinit var edtBookName: TextInputEditText
    private lateinit var edtAuthor: TextInputEditText
    private lateinit var edtIsbn: TextInputEditText
    private lateinit var spinnerCategory: AutoCompleteTextView
    private lateinit var edtCost: TextInputEditText
    private lateinit var btnSave: MaterialButton

    private lateinit var viewModel: AddBookViewModel

    private var categoryList: List<CategoryResponse> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Check quyền hạn (Đặc thù của tính năng thêm sách)
        if (!BookWarehousePermissions.canCreateOrUpdateCatalog(requireContext())) {
            Toast.makeText(requireContext(), "Cần đăng nhập nhân viên hoặc thủ thư để thêm sách.", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }

        initViews(view)
        setupViewModel()
        observeViewModel()
        setupListeners()

        viewModel.fetchCategories()
    }

    private fun initViews(view: View) {
        btnCancel = view.findViewById(R.id.btnCancel)
        edtBookName = view.findViewById(R.id.edtBookName)
        edtAuthor = view.findViewById(R.id.edtAuthor)
        edtIsbn = view.findViewById(R.id.edtIsbn)
        spinnerCategory = view.findViewById(R.id.spinnerCategory)
        edtCost = view.findViewById(R.id.edtCost)
        btnSave = view.findViewById(R.id.btnSave)
    }

    private fun setupViewModel() {
        val retrofit = RetrofitClient.getInstance(requireContext())

        // Khởi tạo cả 2 Service
        val bookApiService = retrofit.create(BookApiService::class.java)
        val categoryApiService = retrofit.create(CategoryApiService::class.java)

        // Khởi tạo cả 2 Repository
        val bookRepository = BookRepository(bookApiService)
        val categoryRepository = CategoryRepository(categoryApiService)

        // Truyền cả 2 vào ViewModel
        val factory = GenericViewModelFactory { AddBookViewModel(bookRepository, categoryRepository) }
        viewModel = ViewModelProvider(this, factory)[AddBookViewModel::class.java]
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Lắng nghe trạng thái Lưu sách
            launch {
                viewModel.addBookState.collectLatest { state ->
                    when (state) {
                        is AddBookState.Idle -> {
                            btnSave.isEnabled = true
                            btnSave.text = "LƯU SÁCH"
                        }
                        is AddBookState.Loading -> {
                            btnSave.isEnabled = false
                            btnSave.text = "Đang xử lý..."
                        }
                        is AddBookState.Success -> {
                            Toast.makeText(requireContext(), "Thêm sách thành công: ${state.data.title}", Toast.LENGTH_SHORT).show()
                            findNavController().popBackStack()
                        }
                        is AddBookState.Error -> {
                            btnSave.isEnabled = true
                            btnSave.text = "LƯU SÁCH"
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }

            // Lắng nghe danh sách Thể loại để bơm vào Spinner
            launch {
                viewModel.categories.collectLatest { list ->
                    categoryList = list
                    // Chỉ lấy danh sách TÊN thể loại để hiển thị lên UI
                    val categoryNames = list.map { it.name }
                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        categoryNames
                    )
                    spinnerCategory.setAdapter(adapter)
                }
            }
        }
    }

    private fun setupListeners() {
        btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }

        btnSave.setOnClickListener {
            val bookName = edtBookName.text.toString().trim()
            val author = edtAuthor.text.toString().trim()
            val isbn = edtIsbn.text.toString().trim()
            val costStr = edtCost.text.toString().trim()
            val selectedCategoryName = spinnerCategory.text.toString().trim()

            // Validate báo lỗi từng ô
            if (bookName.isEmpty()) {
                edtBookName.error = "Vui lòng nhập tên sách!"
                edtBookName.requestFocus()
                return@setOnClickListener
            }
            if (author.isEmpty()) {
                edtAuthor.error = "Vui lòng nhập tên tác giả!"
                edtAuthor.requestFocus()
                return@setOnClickListener
            }
            if (isbn.isEmpty()) {
                edtIsbn.error = "Vui lòng nhập mã ISBN!"
                edtIsbn.requestFocus()
                return@setOnClickListener
            }
            if (selectedCategoryName.isEmpty()) {
                spinnerCategory.error = "Vui lòng chọn thể loại!"
                spinnerCategory.requestFocus()
                return@setOnClickListener
            }
            if (costStr.isEmpty() || costStr == "VND") {
                edtCost.error = "Vui lòng nhập giá gốc của sách!"
                edtCost.requestFocus()
                return@setOnClickListener
            }

            val libraryId = TokenManager(requireContext()).getLibraryId()
            if (libraryId == null) {
                Toast.makeText(requireContext(), "Lỗi: Không tìm thấy thông tin thư viện!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 💡 BÍ KÍP Ở ĐÂY: Dò tìm ID của Thể loại dựa vào cái Tên mà User vừa chọn
            val selectedCategory = categoryList.find { it.name == selectedCategoryName }
            val categoryIdToSave = selectedCategory?.categoryId

            if (categoryIdToSave == null) {
                Toast.makeText(requireContext(), "Lỗi: Thể loại không hợp lệ!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = BookRequest(
                libraryId = libraryId,
                categoryId = categoryIdToSave, // Giờ nó đã lấy ID thật rồi nè!
                isbn = isbn,
                title = bookName,
                author = author,
                basePrice = costStr.toDouble()
            )

            viewModel.addBook(request)
        }
    }
}