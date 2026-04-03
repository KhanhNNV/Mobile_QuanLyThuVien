package com.example.quanlythuvien.ui.books

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R
import com.example.quanlythuvien.data.entity.Book

class BookListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var bookAdapter: BookAdapter
    private lateinit var spinnerCategory: Spinner

    // Khai báo các biến cho phần Filter
    private lateinit var btnToggleFilter: ImageButton
    private lateinit var llFilterContainer: LinearLayout
    private var isFilterExpanded = false

    private lateinit var btnAddBook : Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_book_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Đổi tên Header
        val tvHeaderTitle = view.findViewById<TextView>(R.id.tvHeaderTitle)
        val tvHeaderSubtitle = view.findViewById<TextView>(R.id.tvHeaderSubtitle)
        tvHeaderTitle?.text = "Kho Sách"
        tvHeaderSubtitle?.text = "Quản lý và cập nhật sách"

        // Ánh xạ View trước
        recyclerView = view.findViewById(R.id.recyclerViewBooks)
        btnToggleFilter = view.findViewById(R.id.btnToggleFilter)
        llFilterContainer = view.findViewById(R.id.llFilterContainer)
        spinnerCategory = view.findViewById(R.id.spinnerCategory)
        btnAddBook = view.findViewById(R.id.btnAddBook)

        // Set nút thêm sách
        btnAddBook.setOnClickListener {
            findNavController().navigate(R.id.bookImportFragment)
        }

        // Khởi tạo danh sách và Adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val dummyBooks = createDummyData()
        bookAdapter = BookAdapter(dummyBooks)

        // Lắng nghe sự kiện click mở Dialog
        bookAdapter.onItemClick = { selectedBook ->
            showBookDetailDialog(selectedBook)
        }

        // Gắn adapter vào RecyclerView
        recyclerView.adapter = bookAdapter

        // Cài đặt các chức năng khác
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

        // Ánh xạ View của Dialog
        val tvTitle = dialog.findViewById<TextView>(R.id.tvDetailTitle)
        val tvAuthor = dialog.findViewById<TextView>(R.id.tvDetailAuthor)
        val tvCategory = dialog.findViewById<TextView>(R.id.tvDetailCategory)
        val tvStatus = dialog.findViewById<TextView>(R.id.tvDetailStatus)
        val rvBookCopies = dialog.findViewById<RecyclerView>(R.id.rvBookCopies)
        val btnClose = dialog.findViewById<Button>(R.id.btnCloseDialog)

        // Đổ thông tin sách gốc
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

        if (book.availableQuantity > 0) {
            tvStatus?.text = "Tổng quan: Còn ${book.availableQuantity} cuốn" // Đã bỏ phần / Tổng
            tvStatus?.setTextColor(resources.getColor(R.color.green, null))
        } else {
            tvStatus?.text = "Tổng quan: Đã hết sách trong kho"
            tvStatus?.setTextColor(resources.getColor(R.color.red, null))
        }

        // Cài đặt RECYCLERVIEW mã sách trong dialog
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

            // Đưa dữ liệu vào danh sách
            copyList.add(BookCopyItem("Mã cuốn: B${book.bookId}-$i", statusText, statusColor))
        }

        // Gắn Adapter vào RecyclerView
        rvBookCopies?.adapter = BookCopyAdapter(copyList)

        btnClose?.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
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
                btnToggleFilter.animate().rotation(90f).setDuration(200).start()
            } else {
                llFilterContainer.visibility = View.GONE
                btnToggleFilter.animate().rotation(0f).setDuration(200).start()
            }
        }
    }

    private fun createDummyData(): List<Book> {
        return listOf(
            Book(bookId = 1, categoryId = 1, isbnCode = "978-0132350884", title = "Clean Code", author = "Robert C. Martin", totalQuantity = 10, availableQuantity = 5),
            Book(bookId = 2, categoryId = 2, isbnCode = "978-6045635094", title = "Đắc Nhân Tâm", author = "Dale Carnegie", totalQuantity = 15, availableQuantity = 12),
            Book(bookId = 3, categoryId = 1, isbnCode = "978-0201633610", title = "Design Patterns", author = "Erich Gamma", totalQuantity = 5, availableQuantity = 2),
            Book(bookId = 4, categoryId = 3, isbnCode = "978-6048554164", title = "Nhà Giả Kim", author = "Paulo Coelho", totalQuantity = 5, availableQuantity = 0, lostQuantity = 1),
            Book(bookId = 5, categoryId = 4, isbnCode = "978-6043026368", title = "Sapiens", author = "Yuval Noah Harari", totalQuantity = 10, availableQuantity = 8),
            Book(bookId = 6, categoryId = 1, isbnCode = "978-0596009205", title = "Head First Java", author = "Kathy Sierra", totalQuantity = 5, availableQuantity = 3)
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