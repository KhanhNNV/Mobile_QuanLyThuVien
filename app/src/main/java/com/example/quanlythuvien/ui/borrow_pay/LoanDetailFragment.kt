package com.example.quanlythuvien.ui.borrow_pay

import android.app.DatePickerDialog
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R
import com.example.quanlythuvien.ui.borrow_pay.data.BookData
import com.example.quanlythuvien.ui.borrow_pay.data.LoanDetailItemData
import com.example.quanlythuvien.ui.borrow_pay.data.LoanItemData
import com.example.quanlythuvien.viewmodel.LoanSharedViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.*

class LoanDetailFragment : Fragment() {

    private val loanSharedViewModel: LoanSharedViewModel by activityViewModels()
    private lateinit var currentItem: LoanItemData
    private lateinit var bookAdapter: LoanDetailAdapter

// ==========================================
// KHAI BÁO CÁC VIEW TRÊN GIAO DIỆN (UI)
// ==========================================

    private lateinit var btnBack: ImageButton
    private lateinit var tvHeaderTitle: TextView
    private lateinit var tvReaderName: TextView
    private lateinit var tvLoanId: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvBorrowDate: TextView

    // Nút 3 chấm của TỔNG PHIẾU MƯỢN
    private lateinit var ibtLoanMenu: ImageButton

    private lateinit var rvBooks: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_loan_detail, container, false)
    }

    private fun initViews(view: View) {
        btnBack = view.findViewById(R.id.btnBack)
        tvHeaderTitle = view.findViewById(R.id.tvHeaderTitle)
        tvReaderName = view.findViewById(R.id.tvDialogReaderName)
        tvLoanId = view.findViewById(R.id.tvDialogLoanId)
        tvStatus = view.findViewById(R.id.tvDialogStatus)
        tvBorrowDate = view.findViewById(R.id.tvDialogBorrowDate)

        // Ánh xạ nút 3 chấm của khối thông tin tổng
        ibtLoanMenu = view.findViewById(R.id.ibtSet)

        rvBooks = view.findViewById(R.id.rvBorrowedBooks)
    }

    // Hàm này giúp truyền dữ liệu trong LoanItem cho View
    private fun bindDataToUI() {
        tvReaderName.text = currentItem.readerName
        tvLoanId.text = currentItem.loanId.toString()
        tvBorrowDate.text = currentItem.borrowDate

        editStatusUI(currentItem.overallStatus, tvStatus)
        bookAdapter.submitList(currentItem.borrowedBooks)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)

        // 1. Setup Header
        tvHeaderTitle.text = "Chi tiết phiếu mượn"
        btnBack.setOnClickListener {
            loanSharedViewModel.updatedLoanToSave.value = currentItem
            findNavController().popBackStack()
        }

        // ==========================================
        // 2. LOGIC NÚT 3 CHẤM CỦA TỔNG PHIẾU MƯỢN
        // ==========================================


        // 1. Lấy SharedPreferences
        val sharedPreferences = requireActivity().getSharedPreferences("LibraryAppPrefs", Context.MODE_PRIVATE)
        val currentRole = sharedPreferences.getString("userRole", "STAFF")
        val isAdmin = currentRole == "ADMIN" // Tạo một biến boolean cho dễ dùng

        // 2. Xử lý nút 3 chấm (Xóa tổng phiếu)
        if (isAdmin) {
            ibtLoanMenu.visibility = View.VISIBLE
            ibtLoanMenu.setOnClickListener { menuView ->
                val popup = PopupMenu(requireContext(), menuView)
                popup.menu.add(0, 1, 0, "Xóa phiếu mượn")
                popup.setOnMenuItemClickListener { item ->
                    if (item.itemId == 1) handleDeleteLoan()
                    true
                }
                popup.show()
            }
        } else {
            ibtLoanMenu.visibility = View.GONE
        }

        // 3. Khởi tạo Adapter (Truyền thêm biến isAdmin vào để Adapter tự biết đường ẩn nút)
        bookAdapter = LoanDetailAdapter(isAdmin) { targetBook, action ->
            when (action) {
                "EDIT" -> handleEditBook(targetBook)
                "DELETE" -> {
                    // Check lại 1 lần nữa cho chắc chắn an toàn tuyệt đối
                    if (isAdmin) handleDeleteBook(targetBook)
                }
            }
        }



        rvBooks.layoutManager = LinearLayoutManager(requireContext())
        rvBooks.adapter = bookAdapter

        // Nhận dữ liệu lúc mới mở Fragment lên
        loanSharedViewModel.selectedLoanToView.value?.let { item ->
            currentItem = item
            bindDataToUI()
        }
    }

// ==========================================
// CÁC HÀM XỬ LÝ LOGIC CHO TỔNG PHIẾU MƯỢN
// ==========================================

    private fun handleDeleteLoan() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Xác nhận xóa vĩnh viễn")
            .setMessage("Cảnh báo: Việc xóa phiếu mượn của '${currentItem.readerName}' sẽ làm mất toàn bộ lịch sử mượn và trạng thái các cuốn sách trong phiếu này. Bạn có chắc chắn muốn tiếp tục?")
            .setIcon(R.drawable.ic_diamond_exclamation) // Đảm bảo bạn có icon này hoặc đổi icon khác
            .setNegativeButton("Hủy", null)
            .setPositiveButton("Xóa vĩnh viễn") { _, _ ->
                // Phát tín hiệu xóa ID phiếu về cho BorrowPayFragment
                loanSharedViewModel.deletedLoanId.value = currentItem.loanId

                Toast.makeText(requireContext(), "Đã xóa phiếu mượn thành công!", Toast.LENGTH_SHORT).show()

                // Quay lại màn hình danh sách
                findNavController().popBackStack()
            }
            .show()
    }
// ==========================================
// CÁC HÀM XỬ LÝ LOGIC CHO TỪNG CUỐN SÁCH
// ==========================================
private fun handleEditBook(targetBook: LoanDetailItemData) {
    val dialog = BottomSheetDialog(requireContext())
    val viewDialog = layoutInflater.inflate(R.layout.layout_dialog_edit_loan_detail, null)
    dialog.setContentView(viewDialog)

    // 1. Ánh xạ View
    val spnSelectBookInLoan = viewDialog.findViewById<Spinner>(R.id.spnSelectBookInLoan)
    val spnStatus = viewDialog.findViewById<Spinner>(R.id.spnEditStatus)
    val edtDueDate = viewDialog.findViewById<EditText>(R.id.edtEditDueDate)
    val btnSave = viewDialog.findViewById<Button>(R.id.btnSaveEdit)
    val btnCancel = viewDialog.findViewById<Button>(R.id.btnCancelEdit)

    // ==========================================
    // 2. CHUẨN BỊ DỮ LIỆU CHO SPINNER CHỌN SÁCH
    // ==========================================
    // Chuyển đổi List<BookData> thành List<LoanDetailItemData> để truyền vào Adapter
    val sampleBooks = getSampleLibraryBooks().mapIndexed { index, bookData ->
        LoanDetailItemData(
            bookId = 1000L + index, // Cấp phát ID giả định (1000, 1001, 1002...)
            title = bookData.title,
            author = bookData.author,
            categoryName = bookData.categoryName,
            dueDate = "",
            returnDate = null,
            status = "AVAILABLE"
        )
    }

    // Tạo danh sách gộp cho Spinner
    val combinedBookOptions = mutableListOf<LoanDetailItemData>()

    // Vị trí số 0: Cuốn sách đang được chọn để sửa (thêm chữ "Giữ nguyên" để người dùng biết)
    val currentBookOption = targetBook.copy(title = "${targetBook.title} (Giữ nguyên)")
    combinedBookOptions.add(currentBookOption)

    // Từ vị trí số 1 trở đi: Danh sách sách mẫu trong kho
    combinedBookOptions.addAll(sampleBooks)

    // Cài đặt Adapter
    val customBookAdapter = CustomBookSpinnerAdapter(requireContext(), combinedBookOptions)
    spnSelectBookInLoan.adapter = customBookAdapter
    spnSelectBookInLoan.setSelection(0) // Luôn mặc định chọn cuốn hiện tại

    // ==========================================
    // 3. THIẾT LẬP TRẠNG THÁI VÀ HẠN TRẢ
    // ==========================================
    val statusAdapter = ArrayAdapter(
        requireContext(),
        android.R.layout.simple_spinner_item,
        listOf("Đang mượn", "Đã trả", "Bị mất")
    )
    statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    spnStatus.adapter = statusAdapter

    // Trạng thái mặc định lấy từ cuốn sách gốc
    spnStatus.setSelection(when (targetBook.status) {
        "BORROWING" -> 0
        "RETURNED" -> 1
        "LOST" -> 2
        else -> 0
    })

    edtDueDate.setText(targetBook.dueDate)
    edtDueDate.setOnClickListener {
        val calendar = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            val newDate = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year)
            edtDueDate.setText(newDate)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    // ==========================================
    // 4. LOGIC LƯU THAY ĐỔI VÀO PHIẾU MƯỢN
    // ==========================================
    btnSave.setOnClickListener {
        val selectedIndex = spnSelectBookInLoan.selectedItemPosition

        // Lấy ra thông tin của cuốn sách người dùng CHỐT
        // Nếu chọn 0 -> Lấy cuốn gốc (targetBook). Nếu > 0 -> Lấy cuốn sách mẫu
        val finalBookInfo = if (selectedIndex == 0) targetBook else combinedBookOptions[selectedIndex]

        val newDueDate = edtDueDate.text.toString()
        val newStatusStr = when (spnStatus.selectedItemPosition) {
            1 -> "RETURNED"
            2 -> "LOST"
            else -> "BORROWING"
        }

        // Tiến hành cập nhật danh sách sách mượn
        val updatedBooks = currentItem.borrowedBooks.map { book ->
            // Chỉ tìm và ghi đè ĐÚNG vào vị trí của cuốn sách mà ta đang bấm "Sửa"
            if (book.bookId == targetBook.bookId) {
                val newReturnDate = if (newStatusStr == "RETURNED" && book.status != "RETURNED") {
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                } else if (newStatusStr != "RETURNED") {
                    null
                } else {
                    book.returnDate
                }

                // Ghi đè thông tin mới
                book.copy(
                    bookId = finalBookInfo.bookId, // Thay đổi ID nếu đổi sách khác
                    title = finalBookInfo.title.replace(" (Giữ nguyên)", ""), // Làm sạch tên sách
                    author = finalBookInfo.author, // Tác giả mới (hoặc cũ)
                    categoryName = finalBookInfo.categoryName, // Thể loại mới (hoặc cũ)
                    dueDate = newDueDate,
                    status = newStatusStr,
                    returnDate = newReturnDate
                )
            } else {
                book // Các cuốn khác nằm trong phiếu mượn thì giữ nguyên
            }
        }.toMutableList()

        // Kiểm tra trạng thái tổng quát (Nếu có bất kỳ cuốn nào chưa trả -> BORROWING)
        val hasAnyActive = updatedBooks.any { it.status == "BORROWING" || it.status == "LOST" }
        val newOverallStatus = if (hasAnyActive || updatedBooks.isEmpty()) "BORROWING" else "RETURNED"

        // Gán dữ liệu đã cập nhật vào Object chính
        currentItem = currentItem.copy(
            borrowedBooks = updatedBooks,
            overallStatus = newOverallStatus
        )

        // Làm mới giao diện bên ngoài
        editStatusUI(currentItem.overallStatus, tvStatus)
        bookAdapter.submitList(currentItem.borrowedBooks)

        Toast.makeText(requireContext(), "Cập nhật sách thành công!", Toast.LENGTH_SHORT).show()
        dialog.dismiss()
    }

    btnCancel.setOnClickListener { dialog.dismiss() }
    dialog.show()
}

    //Hàm xóa sách đã mượn
    private fun handleDeleteBook(targetBook: LoanDetailItemData) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Xác nhận xóa sách")
            .setMessage("Bạn có chắc chắn muốn xóa cuốn '${targetBook.title}' khỏi phiếu mượn này không?")
            .setNegativeButton("Hủy", null)
            .setPositiveButton("Xóa") { _, _ ->

                val updatedBooks = currentItem.borrowedBooks.filter { it.bookId != targetBook.bookId }.toMutableList()

                val hasAnyBookBorrowing = updatedBooks.any {
                    it.status == "BORROWING" || it.status == "LOST"
                }
                val newOverallStatus = if (hasAnyBookBorrowing || updatedBooks.isEmpty()) "BORROWING" else "RETURNED"

                currentItem = currentItem.copy(
                    borrowedBooks = updatedBooks,
                    overallStatus = newOverallStatus
                )
                editStatusUI(currentItem.overallStatus, tvStatus)
                bookAdapter.submitList(currentItem.borrowedBooks)

                Toast.makeText(requireContext(), "Đã xóa sách khỏi phiếu!", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    //Hàm chỉnh màu cho LoanStatus
    private fun editStatusUI(status: String, tvStatus: TextView) {
        val context = requireContext()

        // Sử dụng when để code sạch và dễ mở rộng hơn if-else
        when (status) {
            "BORROWING" -> {
                tvStatus.text = "Đang mượn"
                tvStatus.setTextColor(ContextCompat.getColor(context, R.color.text_status_info))
                tvStatus.setBackgroundResource(R.drawable.bg_status_info)
            }
            "RETURNED" -> {
                tvStatus.text = "Đã trả"
                tvStatus.setTextColor(ContextCompat.getColor(context, R.color.text_status_success))
                tvStatus.setBackgroundResource(R.drawable.bg_status_success)
            }
            "OVERDUE" -> {
                tvStatus.text = "Quá hạn"
                // Sử dụng màu đỏ và background đỏ cho trạng thái quá hạn
                tvStatus.setTextColor(ContextCompat.getColor(context, R.color.text_status_error))
                tvStatus.setBackgroundResource(R.drawable.bg_status_error)
            }
        }
    }
    // ==========================================
    // TẠO DỮ LIỆU MẪU: DANH SÁCH SÁCH TRONG KHO
    // ==========================================
    // Hàm tạo dữ liệu mẫu mô phỏng Database của thư viện
    fun getSampleLibraryBooks(): List<BookData> {
        return listOf(
            // --- Nhóm Công nghệ / Lập trình ---
            BookData("Lập trình Android với Kotlin", "Nguyễn Văn A", "Công nghệ thông tin"),
            BookData("Clean Code: Mã sạch", "Robert C. Martin", "Kỹ thuật phần mềm"),
            BookData("Cấu trúc dữ liệu và giải thuật", "Phạm Văn Ất", "Giáo trình ĐH"),
            BookData("Design Patterns in Java", "Gang of Four", "Kỹ thuật phần mềm"),
            BookData("Head First Java", "Kathy Sierra", "Công nghệ thông tin"),
            BookData("Làm chủ trí tuệ nhân tạo (AI)", "Nhiều tác giả", "Khoa học máy tính"),

            // --- Nhóm Văn học / Tiểu thuyết ---
            BookData("Nhà Giả Kim", "Paulo Coelho", "Tiểu thuyết"),
            BookData("Dế Mèn Phiêu Lưu Ký", "Tô Hoài", "Văn học Việt Nam"),
            BookData("Hai Số Phận", "Jeffrey Archer", "Tiểu thuyết nước ngoài"),
            BookData("Mắt Biếc", "Nguyễn Nhật Ánh", "Truyện dài"),
            BookData("Sherlock Holmes toàn tập", "Arthur Conan Doyle", "Trinh thám"),

            // --- Nhóm Kỹ năng / Phát triển bản thân ---
            BookData("Đắc Nhân Tâm", "Dale Carnegie", "Kỹ năng sống"),
            BookData("Tuổi Trẻ Đáng Giá Bao Nhiêu", "Rosie Nguyễn", "Phát triển bản thân"),
            BookData("Lối Sống Tối Giản Của Người Nhật", "Sasaki Fumio", "Kỹ năng sống"),
            BookData("Atomic Habits (Thói quen nguyên tử)", "James Clear", "Tâm lý học"),

            // --- Nhóm Lịch sử / Khoa học Xã hội ---
            BookData("Sapiens: Lược Sử Loài Người", "Yuval Noah Harari", "Lịch sử"),
            BookData("Tâm Lý Học Tội Phạm", "Stanton Samenow", "Tâm lý học"),
            BookData("Súng, Vi Trùng và Thép", "Jared Diamond", "Khoa học xã hội")
        )
    }


    // =========================================================================
    // ADAPTER CUSTOM CHO SPINNER CHỌN SÁCH
    // =========================================================================
    inner class CustomBookSpinnerAdapter(
        context: android.content.Context,
        private val bookList: List<LoanDetailItemData>
    ) : ArrayAdapter<LoanDetailItemData>(context, 0, bookList) {

        // Giao diện khi Spinner đang đóng (chỉ hiện 1 mục đã chọn)
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            return createView(position, convertView, parent)
        }

        // Giao diện khi bấm vào Spinner (xổ ra danh sách để chọn)
        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            return createView(position, convertView, parent)
        }

        private fun createView(position: Int, convertView: View?, parent: ViewGroup): View {
            // Nạp layout item_custom_spinner.xml của bạn
            val view = convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.item_custom_spinner_book, parent, false)

            val item = getItem(position)

            // Ánh xạ các View trong item_custom_spinner.xml
            val tvName = view.findViewById<TextView>(R.id.tvBookName)
            val tvAuthorCategory = view.findViewById<TextView>(R.id.tvAuthorCategory)

            item?.let {
                // Hiển thị tên sách (in đậm)
                tvName.text = "${it.bookId} - ${it.title}"

                // Hiển thị Tác giả & Thể loại (nếu bạn dùng bản XML mới nhất mình gửi)
                // Dùng if (tvAuthorCategory != null) để tránh lỗi nếu bạn dùng bản XML cũ
                if (tvAuthorCategory != null) {
                    tvAuthorCategory.text = "${it.author}  •  ${it.categoryName}"
                }
            }

            return view
        }
    }
}