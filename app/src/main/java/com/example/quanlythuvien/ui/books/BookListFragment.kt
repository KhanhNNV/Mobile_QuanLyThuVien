package com.example.quanlythuvien.ui.books

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.fragment.app.Fragment
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
    private var isFilterExpanded = false // Biến theo dõi trạng thái đang mở hay đóng

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_book_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerViewBooks)
        btnToggleFilter = view.findViewById(R.id.btnToggleFilter)
        llFilterContainer = view.findViewById(R.id.llFilterContainer)
        spinnerCategory = view.findViewById(R.id.spinnerCategory)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val dummyBooks = createDummyData()
        bookAdapter = BookAdapter(dummyBooks)
        recyclerView.adapter = bookAdapter

        setupFilterToggle()
        setupCategorySpinner()
    }

    // Hàm cài đặt dữ liệu cho Dropdown Danh mục
    private fun setupCategorySpinner() {
        val categories = listOf("Tất cả danh mục", "CNTT", "Tâm lý", "Tiểu thuyết", "Lịch sử")

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinnerCategory.adapter = adapter

    }

    // Hàm riêng để xử lý logic đóng/mở bộ lọc
    private fun setupFilterToggle() {
        btnToggleFilter.setOnClickListener {
            isFilterExpanded = !isFilterExpanded // Đảo ngược trạng thái

            if (isFilterExpanded) {
                // Đang ẩn -> Mở ra
                llFilterContainer.visibility = View.VISIBLE

                // Xoay icon mũi tên hướng xuống (xoay 90 độ)
                btnToggleFilter.animate().rotation(90f).setDuration(200).start()
            } else {
                // Đang mở -> Ẩn đi
                llFilterContainer.visibility = View.GONE

                // Xoay icon mũi tên trở về ban đầu (0 độ)
                btnToggleFilter.animate().rotation(0f).setDuration(200).start()
            }
        }
    }

    // Hàm tạo dữ liệu mẫu để test UI
    private fun createDummyData(): List<Book> {
        return listOf(
            Book(
                bookId = 1,
                categoryId = 1, // CNTT (Giả sử ID là 1)
                isbnCode = "978-0132350884",
                title = "Clean Code: A Handbook of Agile Software Craftsmanship",
                author = "Robert C. Martin",
                totalQuantity = 10,
                availableQuantity = 5
            ),
            Book(
                bookId = 2,
                categoryId = 2, // Tâm lý (Giả sử ID là 2)
                isbnCode = "978-6045635094",
                title = "Đắc Nhân Tâm",
                author = "Dale Carnegie",
                totalQuantity = 15,
                availableQuantity = 12
            ),
            Book(
                bookId = 3,
                categoryId = 1, // CNTT
                isbnCode = "978-0201633610",
                title = "Design Patterns: Elements of Reusable Object-Oriented Software",
                author = "Erich Gamma",
                totalQuantity = 5,
                availableQuantity = 2
            ),
            Book(
                bookId = 4,
                categoryId = 3, // Tiểu thuyết (Giả sử ID là 3)
                isbnCode = "978-6048554164",
                title = "Nhà Giả Kim",
                author = "Paulo Coelho",
                totalQuantity = 5,
                availableQuantity = 0, // Đã hết sách
                lostQuantity = 1       // Bị mất 1 cuốn
            ),
            Book(
                bookId = 5,
                categoryId = 4, // Lịch sử (Giả sử ID là 4)
                isbnCode = "978-6043026368",
                title = "Sapiens: Lược sử loài người",
                author = "Yuval Noah Harari",
                totalQuantity = 10,
                availableQuantity = 8
            ),
            Book(
                bookId = 6,
                categoryId = 1, // CNTT
                isbnCode = "978-0596009205",
                title = "Head First Java",
                author = "Kathy Sierra",
                totalQuantity = 5,
                availableQuantity = 3
            )
        )
    }
}