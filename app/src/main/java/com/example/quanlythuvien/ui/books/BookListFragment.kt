package com.example.quanlythuvien.ui.books

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R
import com.example.quanlythuvien.core.api.RetrofitClient
import com.example.quanlythuvien.data.remote.BookApiService
import com.example.quanlythuvien.data.remote.BookCopyApiService
import com.example.quanlythuvien.data.remote.CategoryApiService
import com.example.quanlythuvien.data.repository.BookCopyRepository
import com.example.quanlythuvien.data.repository.BookRepository
import com.example.quanlythuvien.data.repository.CategoryRepository
import com.example.quanlythuvien.utils.BookWarehousePermissions
import com.example.quanlythuvien.utils.GenericViewModelFactory
import com.example.quanlythuvien.utils.TokenManager
import com.example.quanlythuvien.utils.setupCustomHeader
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BookListFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var bookAdapter: BookAdapter
    private lateinit var spinnerCategory: Spinner
    private lateinit var etSearch: EditText
    private lateinit var rgStatusFilter: RadioGroup
    private lateinit var btnResetFilter: Button
    private lateinit var btnApplyFilter: Button
    private lateinit var btnToggleFilter: ImageView
    private lateinit var llFilterContainer: LinearLayout
    private lateinit var layoutEmptyState: LinearLayout
    private lateinit var tvEmptyState: TextView
    private lateinit var btnRetryLoadBooks: MaterialButton
    private lateinit var fabAddBook: FloatingActionButton
    private lateinit var viewModel: BookListViewModel
    private var isFilterExpanded = false
    private var categoryOptions: List<Pair<Long?, String>> = listOf(null to "Tat ca danh muc")
    private var selectedCategoryId: Long? = null
    private var hasLoadError = false
    private var isUpdatingBook = false
    private var currentDialog: android.app.Dialog? = null
    private var currentDialogBookId: Long? = null
    private var currentStatusText: TextView? = null
    private var currentCopyAdapter: BookCopyAdapter? = null
    private val deletingCopyIds = mutableSetOf<Long>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_book_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        applyWarehouseUiPermissions(view)
        setupRecyclerView()
        setupViewModel()
        setupFilterToggle()
        setupCategorySpinner(categoryOptions)
        setupFilterActions()
        observeViewModel()
        viewModel.loadData()
    }

    override fun onResume() {
        super.onResume()
        view?.let { applyWarehouseUiPermissions(it) }
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerViewBooks)
        btnToggleFilter = view.findViewById(R.id.btnToggleFilter)
        llFilterContainer = view.findViewById(R.id.llFilterContainer)
        spinnerCategory = view.findViewById(R.id.spinnerCategory)
        etSearch = view.findViewById(R.id.etSearch)
        rgStatusFilter = view.findViewById(R.id.rgStatusFilter)
        btnResetFilter = view.findViewById(R.id.btnResetFilter)
        btnApplyFilter = view.findViewById(R.id.btnApplyFilter)
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState)
        tvEmptyState = view.findViewById(R.id.tvEmptyState)
        btnRetryLoadBooks = view.findViewById(R.id.btnRetryLoadBooks)
        fabAddBook = view.findViewById(R.id.fabAddBook)
        btnRetryLoadBooks.setOnClickListener {
            viewModel.loadData()
        }
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        bookAdapter = BookAdapter()
        bookAdapter.onItemClick = { selectedBook ->
            viewModel.loadBookDetail(selectedBook.bookId)
        }
        recyclerView.adapter = bookAdapter
    }

    private fun setupViewModel() {
        val retrofit = RetrofitClient.getInstance(requireContext())
        val bookApiService = retrofit.create(BookApiService::class.java)
        val bookCopyApiService = retrofit.create(BookCopyApiService::class.java)
        val categoryApiService = retrofit.create(CategoryApiService::class.java)
        val repository = BookRepository(bookApiService)
        val bookCopyRepository = BookCopyRepository(bookCopyApiService)
        val categoryRepository = CategoryRepository(categoryApiService)
        val libraryId = TokenManager(requireContext()).getLibraryId()
        if (libraryId == null) {
            Toast.makeText(requireContext(), "Không tìm thấy thông tin thư viện.", Toast.LENGTH_LONG).show()
            findNavController().popBackStack()
            return
        }
        val factory = GenericViewModelFactory {
            BookListViewModel(repository, categoryRepository, bookCopyRepository, libraryId)
        }
        viewModel = ViewModelProvider(this, factory)[BookListViewModel::class.java]
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.bookListState.collectLatest { state ->
                        when (state) {
                            BookListUiState.Idle -> {
                                hasLoadError = false
                            }
                            BookListUiState.Loading -> Unit
                            is BookListUiState.Success -> {
                                hasLoadError = false
                            }
                            is BookListUiState.Error -> {
                                hasLoadError = true
                                tvEmptyState.text = state.message
                                btnRetryLoadBooks.visibility = View.VISIBLE
                                layoutEmptyState.visibility = View.VISIBLE
                                recyclerView.visibility = View.GONE
                            }
                        }
                    }
                }
                launch {
                    viewModel.bookDetailState.collectLatest { state ->
                        when (state) {
                            BookDetailUiState.Idle -> Unit
                            BookDetailUiState.Loading -> Unit
                            is BookDetailUiState.Success -> {
                                if (isUpdatingBook) {
                                    isUpdatingBook = false
                                    Toast.makeText(requireContext(), "Cập nhật sách thành công.", Toast.LENGTH_SHORT).show()
                                } else {
                                    showBookDetailDialog(state.book)
                                }
                            }
                            is BookDetailUiState.Error -> {
                                isUpdatingBook = false
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
                launch {
                    viewModel.bookCopyState.collectLatest { state ->
                        when (state) {
                            BookCopyUiState.Idle -> Unit
                            BookCopyUiState.Loading -> Unit
                            is BookCopyUiState.Success -> {
                                if (currentDialogBookId != state.bookId) return@collectLatest
                                deletingCopyIds.clear()
                                val copies = state.copies
                                val availableCount = copies.count { it.status.equals("AVAILABLE", ignoreCase = true) }
                                currentStatusText?.text = "Tổng quan: Còn $availableCount cuốn"
                                val items = copies.map {
                                    BookCopyItem(
                                        copyIdValue = it.copyId,
                                        copyId = "Mã cuốn: ${it.barcode}",
                                        statusText = it.status,
                                        statusColor = resources.getColor(R.color.text_secondary, null)
                                    )
                                }.toMutableList()
                                val allowDeleteCopy = BookWarehousePermissions
                                    .canDeleteBookCopiesInWarehouseUi(requireContext())
                                currentCopyAdapter = BookCopyAdapter(
                                    copyList = items,
                                    allowDelete = allowDeleteCopy
                                ) { item, position ->
                                    if (!allowDeleteCopy) return@BookCopyAdapter
                                    if (deletingCopyIds.contains(item.copyIdValue)) return@BookCopyAdapter
                                    MaterialAlertDialogBuilder(requireContext())
                                        .setTitle("Xóa bản sao")
                                        .setMessage("Bạn chắc chắn muốn xóa ${item.copyId}?")
                                        .setNegativeButton("Hủy", null)
                                        .setPositiveButton("Xóa") { _, _ ->
                                            deletingCopyIds.add(item.copyIdValue)
                                            currentCopyAdapter?.removeAt(position)
                                            viewModel.deleteBookCopy(item.copyIdValue, state.bookId)
                                        }
                                        .show()
                                }
                                val recycler = currentDialog?.findViewById<RecyclerView>(R.id.rvBookCopies)
                                recycler?.adapter = currentCopyAdapter
                            }
                            is BookCopyUiState.Error -> {
                                deletingCopyIds.clear()
                                if (currentDialog?.isShowing == true) {
                                    if (state.message.contains("Không tìm thấy bản sao", ignoreCase = true)) {
                                        currentDialogBookId?.let { viewModel.loadBookCopies(it) }
                                    } else {
                                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }
                    }
                }
                launch {
                    viewModel.filteredBooks.collectLatest { books ->
                        bookAdapter.setItems(books)
                        if (hasLoadError) {
                            return@collectLatest
                        }
                        if (books.isEmpty()) {
                            tvEmptyState.text = "Không có sách phù hợp"
                            btnRetryLoadBooks.visibility = View.GONE
                            layoutEmptyState.visibility = View.VISIBLE
                            recyclerView.visibility = View.GONE
                        } else {
                            btnRetryLoadBooks.visibility = View.GONE
                            layoutEmptyState.visibility = View.GONE
                            recyclerView.visibility = View.VISIBLE
                        }
                    }
                }
                launch {
                    viewModel.categories.collectLatest { categories ->
                        categoryOptions = buildList {
                            add(null to "Tất cả danh mục")
                            categories.forEach { category ->
                                add(category.categoryId to category.name)
                            }
                        }
                        setupCategorySpinner(categoryOptions)
                    }
                }
            }
        }
    }

    private fun setupFilterActions() {
        btnApplyFilter.setOnClickListener {
            viewModel.applyFilters(
                query = etSearch.text?.toString().orEmpty(),
                categoryId = selectedCategoryId
            )
        }

        btnResetFilter.setOnClickListener {
            etSearch.text?.clear()
            rgStatusFilter.check(R.id.rbAll)
            spinnerCategory.setSelection(0)
            selectedCategoryId = null
            viewModel.resetFilters()
        }

        etSearch.setOnEditorActionListener { _, _, _ ->
            viewModel.applyFilters(
                query = etSearch.text?.toString().orEmpty(),
                categoryId = selectedCategoryId
            )
            true
        }
    }

    private fun setupCategorySpinner(options: List<Pair<Long?, String>>) {
        val labels = options.map { it.second }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, labels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter
        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedCategoryId = options.getOrNull(position)?.first
                viewModel.applyFilters(
                    query = etSearch.text?.toString().orEmpty(),
                    categoryId = selectedCategoryId
                )
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedCategoryId = null
            }
        }
    }

    private fun setupFilterToggle() {
        btnToggleFilter.setOnClickListener {
            isFilterExpanded = !isFilterExpanded
            if (isFilterExpanded) {
                llFilterContainer.visibility = View.VISIBLE
                btnToggleFilter.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.btn_primary)
                btnToggleFilter.imageTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.white)
            } else {
                llFilterContainer.visibility = View.GONE
                btnToggleFilter.backgroundTintList = null
                btnToggleFilter.imageTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.btn_primary)
            }
        }
    }

    private fun warehouseHeaderSubtitle(): String = when {
        BookWarehousePermissions.canManageCatalog(requireContext()) -> "Quản lý và cập nhật sách"
        BookWarehousePermissions.canCreateOrUpdateCatalog(requireContext()) ->
            "Nhân viên: thêm và sửa kho (không xóa bản sao)"
        else -> "Xem kho - cần đăng nhập nhân viên hoặc thủ thư"
    }

    private fun applyWarehouseUiPermissions(headerRoot: View) {
        val canCrud = BookWarehousePermissions.canCreateOrUpdateCatalog(requireContext())
        fabAddBook.visibility = if (canCrud) View.VISIBLE else View.GONE
        fabAddBook.setOnClickListener {
            if (!canCrud) {
                Toast.makeText(requireContext(), "Bạn không có quyền thêm sách vào kho.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            findNavController().navigate(R.id.bookAddFragment)
        }
        setupCustomHeader(headerRoot, "Kho Sách", warehouseHeaderSubtitle())
    }

    private fun showBookDetailDialog(book: com.example.quanlythuvien.data.model.response.BookResponse) {
        currentDialog?.dismiss()
        val dialog = android.app.Dialog(requireContext())
        currentDialog = dialog
        currentDialogBookId = book.bookId
        dialog.setContentView(R.layout.dialog_book_detail)
        dialog.window?.setBackgroundDrawable(
            android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT)
        )
        dialog.window?.setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val tvTitle = dialog.findViewById<TextView>(R.id.tvDetailTitle)
        val tvAuthor = dialog.findViewById<TextView>(R.id.tvDetailAuthor)
        val tvCategory = dialog.findViewById<TextView>(R.id.tvDetailCategory)
        val tvBasePrice = dialog.findViewById<TextView>(R.id.tvDetailBasePrice)
        val tvStatus = dialog.findViewById<TextView>(R.id.tvDetailStatus)
        val rvBookCopies = dialog.findViewById<RecyclerView>(R.id.rvBookCopies)
        val btnClose = dialog.findViewById<Button>(R.id.btnCloseDialog)
        val btnEdit = dialog.findViewById<Button>(R.id.btnEditBook)
        val btnAddCopy = dialog.findViewById<Button>(R.id.btnAddCopy)

        val categoryText = book.categoryName ?: viewModel.categoryNameById(book.categoryId) ?: "Chưa cập nhật"
        val priceText = book.basePrice?.let { "${it.toInt()} VND" } ?: "Chưa có"
        tvTitle.text = book.title
        tvAuthor.text = "Tác giả: ${book.author}"
        tvCategory.text = "Thể loại: $categoryText"
        tvBasePrice.text = "Giá gốc: $priceText"
        tvStatus.text = "Tổng quan: Đang tải bản sao..."
        tvStatus.setTextColor(resources.getColor(R.color.text_secondary, null))
        currentStatusText = tvStatus

        val canCrudWarehouse = BookWarehousePermissions.canCreateOrUpdateCatalog(requireContext())
        btnAddCopy.visibility = if (canCrudWarehouse) View.VISIBLE else View.GONE
        btnAddCopy.setOnClickListener {
            if (!canCrudWarehouse) return@setOnClickListener
            showAddCopyDialog(book.bookId, book.title)
        }
        rvBookCopies.layoutManager = LinearLayoutManager(requireContext())
        val placeholderCopies = mutableListOf<BookCopyItem>()
        rvBookCopies.adapter = BookCopyAdapter(
            copyList = placeholderCopies,
            allowDelete = false
        ) { _, _ -> }

        btnClose.setOnClickListener { dialog.dismiss() }
        btnEdit.setOnClickListener {
            dialog.dismiss()
            showEditBookDialog(book)
        }
        dialog.setOnDismissListener {
            if (currentDialog === dialog) {
                currentDialog = null
                currentDialogBookId = null
                currentStatusText = null
                currentCopyAdapter = null
            }
        }
        dialog.show()
        viewModel.loadBookCopies(book.bookId)
    }

    private fun showAddCopyDialog(bookId: Long, bookTitle: String) {
        val addDialog = android.app.Dialog(requireContext())
        addDialog.setContentView(R.layout.dialog_add_book_copy)
        addDialog.window?.setBackgroundDrawable(
            android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT)
        )
        addDialog.window?.setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val tvInfo = addDialog.findViewById<TextView>(R.id.tvAddCopyBookInfo)
        val edtBarcode = addDialog.findViewById<TextInputEditText>(R.id.edtBarcode)
        val spinnerCondition = addDialog.findViewById<Spinner>(R.id.spinnerCondition)
        val btnCancel = addDialog.findViewById<Button>(R.id.btnCancelAddCopy)
        val btnSave = addDialog.findViewById<Button>(R.id.btnSaveCopy)
        tvInfo.text = "Sách: $bookTitle (ID: $bookId)"

        val conditions = listOf("NEW", "GOOD", "FAIR", "POOR")
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, conditions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCondition.adapter = spinnerAdapter

        btnCancel.setOnClickListener { addDialog.dismiss() }
        btnSave.setOnClickListener {
            val barcode = edtBarcode.text?.toString()?.trim().orEmpty()
            val condition = spinnerCondition.selectedItem?.toString().orEmpty()
            if (barcode.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập Barcode!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.createBookCopy(bookId = bookId, barcode = barcode, condition = condition)
            addDialog.dismiss()
        }
        addDialog.show()
    }

    private fun showEditBookDialog(book: com.example.quanlythuvien.data.model.response.BookResponse) {
        val context = requireContext()
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val pad = (16 * resources.displayMetrics.density).toInt()
            setPadding(pad, pad, pad, 0)
        }

        val edtTitle = EditText(context).apply {
            hint = "Tên sách"
            setText(book.title)
        }
        val edtAuthor = EditText(context).apply {
            hint = "Tác giả"
            setText(book.author)
        }
        val edtIsbn = EditText(context).apply {
            hint = "ISBN"
            setText(book.isbn)
        }
        val edtBasePrice = EditText(context).apply {
            hint = "Giá gốc"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText((book.basePrice ?: 0.0).toString())
        }

        container.addView(edtTitle)
        container.addView(edtAuthor)
        container.addView(edtIsbn)
        container.addView(edtBasePrice)

        MaterialAlertDialogBuilder(context)
            .setTitle("Sửa sách")
            .setView(container)
            .setNegativeButton("Hủy", null)
            .setPositiveButton("Lưu") { _, _ ->
                val title = edtTitle.text.toString().trim()
                val author = edtAuthor.text.toString().trim()
                val isbn = edtIsbn.text.toString().trim()
                val basePrice = edtBasePrice.text.toString().trim().toDoubleOrNull()
                if (title.isEmpty() || author.isEmpty() || isbn.isEmpty() || basePrice == null) {
                    Toast.makeText(context, "Vui lòng nhập đầy đủ thông tin hợp lệ.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val categoryId = book.categoryId ?: categoryOptions.firstOrNull { it.first != null }?.first
                if (categoryId == null) {
                    Toast.makeText(context, "Không tìm thấy danh mục hợp lệ để cập nhật.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                isUpdatingBook = true
                viewModel.updateBook(
                    bookId = book.bookId,
                    categoryId = categoryId,
                    isbn = isbn,
                    title = title,
                    author = author,
                    basePrice = basePrice
                )
            }
            .show()
    }
}
