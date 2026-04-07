package com.example.quanlythuvien.ui.books

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R
import com.example.quanlythuvien.data.entity.Book
import com.google.android.material.textfield.TextInputEditText
import android.content.res.ColorStateList
import androidx.core.content.ContextCompat

class BookListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var bookAdapter: BookAdapter
    private lateinit var spinnerCategory: Spinner

    private lateinit var btnToggleFilter: ImageView
    private lateinit var llFilterContainer: LinearLayout
    private var isFilterExpanded = false
    private lateinit var fabAddBook : com.google.android.material.floatingactionbutton.FloatingActionButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_book_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvHeaderTitle = view.findViewById<TextView>(R.id.tvHeaderTitle)
        val tvHeaderSubtitle = view.findViewById<TextView>(R.id.tvHeaderSubtitle)
        tvHeaderTitle?.text = "Kho Sách"
        tvHeaderSubtitle?.text = "Quản lý và cập nhật sách"

        recyclerView = view.findViewById(R.id.recyclerViewBooks)
        btnToggleFilter = view.findViewById(R.id.btnToggleFilter)
        llFilterContainer = view.findViewById(R.id.llFilterContainer)
        spinnerCategory = view.findViewById(R.id.spinnerCategory)
        fabAddBook = view.findViewById(R.id.fabAddBook)

        fabAddBook.setOnClickListener {
            findNavController().navigate(R.id.bookImportFragment)
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val dummyBooks = createDummyData()
        bookAdapter = BookAdapter(dummyBooks)

        bookAdapter.onItemClick = { selectedBook ->
            showBookDetailDialog(selectedBook)
        }

        recyclerView.adapter = bookAdapter
        setupFilterToggle()
        setupCategorySpinner()
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

        // 1. ÁNH XẠ NÚT THÊM BẢN SAO TỪ UI
        val btnAddCopy = dialog.findViewById<Button>(R.id.btnAddCopy)

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

        if (book.availableQuantity > 0) {
            tvStatus?.text = "Tổng quan: Còn ${book.availableQuantity} cuốn"
            tvStatus?.setTextColor(resources.getColor(R.color.green, null))
        } else {
            tvStatus?.text = "Tổng quan: Đã hết sách trong kho"
            tvStatus?.setTextColor(resources.getColor(R.color.red, null))
        }

        rvBookCopies?.layoutManager = LinearLayoutManager(requireContext())
        val copyList = mutableListOf<BookCopyItem>()
        for (i in 1..book.totalQuantity) {
            val statusText: String
            val statusColor: Int
            if (i <= book.availableQuantity) {
                statusText = "Có sẵn"
                statusColor = resources.getColor(R.color.green, null)
            } else if (i == book.totalQuantity && book.totalQuantity > book.availableQuantity) {
                statusText = "Đã mất"
                statusColor = resources.getColor(R.color.red, null)
            } else {
                statusText = "Đang mượn"
                statusColor = resources.getColor(R.color.yellow, null)
            }
            copyList.add(BookCopyItem("Mã cuốn: B${book.bookId}-$i", statusText, statusColor))
        }
        rvBookCopies?.adapter = BookCopyAdapter(copyList)

        btnClose?.setOnClickListener {
            dialog.dismiss()
        }

        // 2. SỰ KIỆN CLICK MỞ DIALOG THÊM BẢN SAO
        btnAddCopy?.setOnClickListener {
            showAddCopyDialog(book)
        }

        dialog.show()
    }

    // 3. HÀM XỬ LÝ DIALOG THÊM BẢN SAO (FORM NHẬP LIỆU)
    private fun showAddCopyDialog(parentBook: Book) {
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

data class BookCopyItem(val copyId: String, val statusText: String, val statusColor: Int)

class BookCopyAdapter(private val copyList: List<BookCopyItem>) : RecyclerView.Adapter<BookCopyAdapter.CopyViewHolder>() {
    class CopyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCopyId: TextView = view.findViewById(R.id.tvCopyId)
        val tvCopyStatus: TextView = view.findViewById(R.id.tvCopyStatus)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CopyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_book_copy, parent, false)
        return CopyViewHolder(view)
    }
    override fun onBindViewHolder(holder: CopyViewHolder, position: Int) {
        val item = copyList[position]
        holder.tvCopyId.text = item.copyId
        holder.tvCopyStatus.text = item.statusText
        holder.tvCopyStatus.setTextColor(item.statusColor)
    }
    override fun getItemCount() = copyList.size
}