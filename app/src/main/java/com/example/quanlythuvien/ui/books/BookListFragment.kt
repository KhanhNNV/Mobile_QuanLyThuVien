package com.example.quanlythuvien.ui.books

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R
import com.example.quanlythuvien.data2.entity.Book
import com.google.android.material.textfield.TextInputEditText
import androidx.core.content.ContextCompat
import com.example.quanlythuvien.utils.BookWarehousePermissions
import com.example.quanlythuvien.utils.setupCustomHeader
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class BookListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var bookAdapter: BookAdapter
    private lateinit var spinnerCategory: Spinner
    private lateinit var etSearch: EditText
    private lateinit var rgStatusFilter: RadioGroup
    private lateinit var btnResetFilter: Button
    private lateinit var btnApplyFilter: Button
    private lateinit var allBooks: MutableList<Book>

    private lateinit var btnToggleFilter: ImageView
    private lateinit var llFilterContainer: LinearLayout
    private var isFilterExpanded = false
    private lateinit var fabAddBook : com.google.android.material.floatingactionbutton.FloatingActionButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_book_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerViewBooks)
        btnToggleFilter = view.findViewById(R.id.btnToggleFilter)
        llFilterContainer = view.findViewById(R.id.llFilterContainer)
        spinnerCategory = view.findViewById(R.id.spinnerCategory)
        etSearch = view.findViewById(R.id.etSearch)
        rgStatusFilter = view.findViewById(R.id.rgStatusFilter)
        btnResetFilter = view.findViewById(R.id.btnResetFilter)
        btnApplyFilter = view.findViewById(R.id.btnApplyFilter)
        fabAddBook = view.findViewById(R.id.fabAddBook)
        applyWarehouseUiPermissions(view)

        fabAddBook.setOnClickListener {
            if (!BookWarehousePermissions.canCreateOrUpdateCatalog(requireContext())) {
                Toast.makeText(requireContext(), "Bạn không có quyền thêm sách vào kho.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            findNavController().navigate(R.id.bookAddFragment)
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        allBooks = createDummyData().toMutableList()
        bookAdapter = BookAdapter(allBooks.toMutableList())

        bookAdapter.onItemClick = { selectedBook ->
            showBookDetailDialog(selectedBook)
        }

        recyclerView.adapter = bookAdapter
        setupFilterToggle()
        setupCategorySpinner()
        setupFilterActions()
    }

    override fun onResume() {
        super.onResume()
        view?.let { applyWarehouseUiPermissions(it) }
    }

    private fun warehouseHeaderSubtitle(): String = when {
        BookWarehousePermissions.canManageCatalog(requireContext()) ->
            "Quản lý và cập nhật sách"
        BookWarehousePermissions.canCreateOrUpdateCatalog(requireContext()) ->
            "Nhân viên: thêm & sửa kho (không xóa bản sao)"
        else ->
            "Xem kho — cần đăng nhập nhân viên hoặc thủ thư"
    }

    /** FAB + subtitle theo quyền */
    private fun applyWarehouseUiPermissions(headerRoot: View) {
        val canCrud = BookWarehousePermissions.canCreateOrUpdateCatalog(requireContext())
        fabAddBook.visibility = if (canCrud) View.VISIBLE else View.GONE
        setupCustomHeader(headerRoot, "Kho Sách", warehouseHeaderSubtitle())
    }

    private fun setupFilterActions() {
        btnApplyFilter.setOnClickListener {
            applyFilters()
        }

        btnResetFilter.setOnClickListener {
            etSearch.text?.clear()
            rgStatusFilter.check(R.id.rbAll)
            spinnerCategory.setSelection(0)
            bookAdapter.setItems(allBooks)
        }

        // Enter/ActionSearch cũng áp dụng lọc
        etSearch.setOnEditorActionListener { _, _, _ ->
            applyFilters()
            true
        }
    }

    private fun applyFilters() {
        val query = etSearch.text?.toString()?.trim().orEmpty().lowercase()

        val selectedCategoryId = when (spinnerCategory.selectedItemPosition) {
            1 -> 1 // CNTT
            2 -> 2 // Tâm lý
            3 -> 3 // Tiểu thuyết
            4 -> 4 // Lịch sử
            else -> null // Tất cả
        }

        val filtered = allBooks.filter { book ->
            val matchesQuery =
                query.isEmpty() ||
                    book.title.lowercase().contains(query) ||
                    book.author.lowercase().contains(query) ||
                    book.isbnCode.lowercase().contains(query)

            val matchesCategory =
                selectedCategoryId == null || book.categoryId == selectedCategoryId

            val matchesStatus = when (rgStatusFilter.checkedRadioButtonId) {
                R.id.rbAvailable -> book.availableQuantity > 0
                R.id.rbBorrowed -> (book.totalQuantity - book.availableQuantity - book.lostQuantity) > 0
                R.id.rbLost -> book.lostQuantity > 0
                else -> true // rbAll
            }

            matchesQuery && matchesCategory && matchesStatus
        }

        bookAdapter.setItems(filtered)

        if (filtered.isEmpty()) {
            Toast.makeText(requireContext(), "Không tìm thấy sách phù hợp!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showBookDetailDialog(book: Book) {
        val dialog = android.app.Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_book_detail)

        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
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

        // 1. ÁNH XẠ NÚT THÊM BẢN SAO TỪ UI
        val btnAddCopy = dialog.findViewById<Button>(R.id.btnAddCopy)
        val canCrudWarehouse = BookWarehousePermissions.canCreateOrUpdateCatalog(requireContext())
        btnEdit?.visibility = if (canCrudWarehouse) View.VISIBLE else View.GONE
        btnAddCopy?.visibility = if (canCrudWarehouse) View.VISIBLE else View.GONE

        val categoryName = when (book.categoryId) {
            1 -> "Công nghệ thông tin (CNTT)"
            2 -> "Tâm lý"
            3 -> "Tiểu thuyết"
            4 -> "Lịch sử"
            else -> "Danh mục khác"
        }

        tvTitle?.text = book.title
        tvAuthor?.text = "Tác giả: ${book.author}"
        tvCategory?.text = "Thể loại: $categoryName"
        tvBasePrice?.text = "Giá gốc: ${book.basePrice.toInt()} VND"

        val availableCount = book.availableQuantity.coerceAtLeast(0)
        val lostCount = book.lostQuantity.coerceAtLeast(0)
        val borrowedCount = (book.totalQuantity - availableCount - lostCount).coerceAtLeast(0)
        tvStatus?.text = "Tổng quan: Có sẵn $availableCount • Đang mượn $borrowedCount • Đã mất $lostCount"
        tvStatus?.setTextColor(
            resources.getColor(
                if (availableCount > 0) R.color.green else R.color.red,
                null
            )
        )

        rvBookCopies?.layoutManager = LinearLayoutManager(requireContext())
        val copyList = mutableListOf<BookCopyItem>()
        val maxLost = lostCount.coerceAtMost(book.totalQuantity)
        val maxAvailable = availableCount.coerceAtMost((book.totalQuantity - maxLost).coerceAtLeast(0))
        val maxBorrowed = (book.totalQuantity - maxAvailable - maxLost).coerceAtLeast(0)

        for (i in 1..book.totalQuantity) {
            val statusText: String
            val statusColor: Int
            if (i <= maxAvailable) {
                statusText = "Có sẵn"
                statusColor = resources.getColor(R.color.green, null)
            } else if (i <= maxAvailable + maxBorrowed) {
                statusText = "Đang mượn"
                statusColor = resources.getColor(R.color.yellow, null)
            } else {
                statusText = "Đã mất"
                statusColor = resources.getColor(R.color.red, null)
            }
            copyList.add(BookCopyItem("Mã cuốn: B${book.bookId}-$i", statusText, statusColor))
        }
        lateinit var copyAdapter: BookCopyAdapter
        val allowDeleteCopy = BookWarehousePermissions.canDeleteBookCopiesInWarehouseUi(requireContext())
        copyAdapter = BookCopyAdapter(copyList, allowDelete = allowDeleteCopy) { item, position ->
            // Validation: đang mượn thì không cho xóa
            if (item.statusText == "Đang mượn") {
                Toast.makeText(requireContext(), "Bản sao đang mượn — không thể xóa!", Toast.LENGTH_SHORT).show()
                return@BookCopyAdapter
            }

            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Xóa bản sao")
                .setMessage("Bạn chắc chắn muốn xóa ${item.copyId}?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa") { _, _ ->
                    copyAdapter.removeAt(position)
                }
                .show()
        }
        rvBookCopies?.adapter = copyAdapter

        btnClose?.setOnClickListener { dialog.dismiss() }

        btnEdit?.setOnClickListener {
            showEditBookDialog(book) {
                // refresh theo bộ lọc hiện tại
                applyFilters()
            }
        }

        // 2. SỰ KIỆN CLICK MỞ DIALOG THÊM BẢN SAO
        btnAddCopy?.setOnClickListener {
            showAddCopyDialog(book)
        }

        dialog.show()
    }

    private fun showEditBookDialog(book: Book, onUpdated: () -> Unit) {
        if (!BookWarehousePermissions.canCreateOrUpdateCatalog(requireContext())) return
        val context = requireContext()

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val pad = (16 * resources.displayMetrics.density).toInt()
            setPadding(pad, pad, pad, 0)
        }

        fun input(hint: String, value: String, inputType: Int): EditText {
            return EditText(context).apply {
                this.hint = hint
                setText(value)
                this.inputType = inputType
            }
        }

        val edtTitle = input("Tên sách", book.title, android.text.InputType.TYPE_CLASS_TEXT)
        val edtAuthor = input("Tác giả", book.author, android.text.InputType.TYPE_CLASS_TEXT)
        val edtIsbn = input("ISBN", book.isbnCode, android.text.InputType.TYPE_CLASS_TEXT)
        val edtBasePrice = input(
            "Giá gốc (VND)",
            book.basePrice.toInt().toString(),
            android.text.InputType.TYPE_CLASS_NUMBER
        )

        container.addView(edtTitle)
        container.addView(edtAuthor)
        container.addView(edtIsbn)
        container.addView(edtBasePrice)

        MaterialAlertDialogBuilder(context)
            .setTitle("Sửa sách")
            .setView(container)
            .setNegativeButton("Hủy", null)
            .setPositiveButton("Lưu") { _, _ ->
                val newTitle = edtTitle.text.toString().trim()
                val newAuthor = edtAuthor.text.toString().trim()
                val newIsbn = edtIsbn.text.toString().trim()
                val newBasePrice = edtBasePrice.text.toString().trim().toDoubleOrNull()

                if (newTitle.isEmpty() || newAuthor.isEmpty() || newIsbn.isEmpty() || newBasePrice == null) {
                    Toast.makeText(context, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val idx = allBooks.indexOfFirst { it.bookId == book.bookId }
                if (idx >= 0) {
                    allBooks[idx] = allBooks[idx].copy(
                        title = newTitle,
                        author = newAuthor,
                        isbnCode = newIsbn,
                        basePrice = newBasePrice
                    )
                    onUpdated()
                }
            }
            .show()
    }

    // 3. HÀM XỬ LÝ DIALOG THÊM BẢN SAO (FORM NHẬP LIỆU)
    private fun showAddCopyDialog(parentBook: Book) {
        if (!BookWarehousePermissions.canCreateOrUpdateCatalog(requireContext())) return
        val addDialog = android.app.Dialog(requireContext())
        addDialog.setContentView(R.layout.dialog_add_book_copy)

        addDialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
        addDialog.window?.setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val tvInfo = addDialog.findViewById<TextView>(R.id.tvAddCopyBookInfo)
        val edtBarcode = addDialog.findViewById<TextInputEditText>(R.id.edtBarcode)
        val spinnerCondition = addDialog.findViewById<Spinner>(R.id.spinnerCondition)
        val btnCancel = addDialog.findViewById<Button>(R.id.btnCancelAddCopy)
        val btnSave = addDialog.findViewById<Button>(R.id.btnSaveCopy)

        // Set Book ID tự động từ cuốn sách cha
        tvInfo.text = "Sách: ${parentBook.title} (ID: ${parentBook.bookId})"

        // Khởi tạo danh sách tình trạng cho Spinner (Dropdown)
        val conditions = listOf("NEW", "GOOD", "FAIR", "POOR")
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, conditions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCondition.adapter = spinnerAdapter

        // Hủy bỏ thêm
        btnCancel.setOnClickListener {
            addDialog.dismiss()
        }

        // Nhấn Lưu
        btnSave.setOnClickListener {
            val barcode = edtBarcode.text.toString().trim()
            val condition = spinnerCondition.selectedItem.toString()
            val status = "AVAILABLE" // Mặc định ở background

            if (barcode.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập Barcode!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Ghi nhận thành công (Sau này bạn ghép lệnh gọi ViewModel/Room Database vào đây)
            Toast.makeText(requireContext(), "Nhập kho thành công!\nBarcode: $barcode\nTình trạng: $condition", Toast.LENGTH_LONG).show()
            addDialog.dismiss()
        }

        addDialog.show()
    }

    private fun setupCategorySpinner() {
        val categories = listOf("Tất cả danh mục", "CNTT", "Tâm lý", "Tiểu thuyết", "Lịch sử")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter
    }

    private fun setupFilterToggle() {
        btnToggleFilter.setOnClickListener {
            isFilterExpanded = !isFilterExpanded
            if (isFilterExpanded) {
                llFilterContainer.visibility = View.VISIBLE
                val activeBgColor = ContextCompat.getColorStateList(requireContext(), R.color.btn_primary)
                btnToggleFilter.backgroundTintList = activeBgColor

                val activeIconColor = ContextCompat.getColorStateList(requireContext(), R.color.white)
                btnToggleFilter.imageTintList = activeIconColor
            } else {
                llFilterContainer.visibility = View.GONE
                btnToggleFilter.backgroundTintList = null

                val defaultIconColor = ContextCompat.getColorStateList(requireContext(), R.color.btn_primary)
                btnToggleFilter.imageTintList = defaultIconColor
            }
        }
    }

    private fun createDummyData(): List<Book> {
        return listOf(
            Book(bookId = 1, categoryId = 1, isbnCode = "978-0132350884", title = "Clean Code", author = "Robert C. Martin", totalQuantity = 10, availableQuantity = 5, basePrice = 250000.0),
            Book(bookId = 2, categoryId = 2, isbnCode = "978-6045635094", title = "Đắc Nhân Tâm", author = "Dale Carnegie", totalQuantity = 15, availableQuantity = 12, basePrice = 120000.0),
            Book(bookId = 3, categoryId = 1, isbnCode = "978-0201633610", title = "Design Patterns", author = "Erich Gamma", totalQuantity = 5, availableQuantity = 2, basePrice = 300000.0),
            Book(bookId = 4, categoryId = 3, isbnCode = "978-6048554164", title = "Nhà Giả Kim", author = "Paulo Coelho", totalQuantity = 5, availableQuantity = 0, lostQuantity = 1, basePrice = 90000.0),
            Book(bookId = 5, categoryId = 4, isbnCode = "978-6043026368", title = "Sapiens", author = "Yuval Noah Harari", totalQuantity = 10, availableQuantity = 8, basePrice = 280000.0),
            Book(bookId = 6, categoryId = 1, isbnCode = "978-0596009205", title = "Head First Java", author = "Kathy Sierra", totalQuantity = 5, availableQuantity = 3, basePrice = 220000.0)
        )
    }
}