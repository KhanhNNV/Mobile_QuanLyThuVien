package com.example.quanlythuvien.ui.borrow_pay

import android.app.DatePickerDialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
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
import com.example.quanlythuvien.ui.borrow_pay.data.LoanDetailItemData
import com.example.quanlythuvien.ui.borrow_pay.data.LoanItemData
import com.example.quanlythuvien.viewmodel.LoanSharedViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.*

class LoanFragment : Fragment() {

    private val loanSharedViewModel: LoanSharedViewModel by activityViewModels()
    private lateinit var currentItem: LoanItemData
    private lateinit var bookAdapter: LoanAdapter

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
        return inflater.inflate(R.layout.fragment_loan, container, false)
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
        ibtLoanMenu.setOnClickListener { menuView ->
            val popup = PopupMenu(requireContext(), menuView)
            popup.menu.add(0, 1, 0, "Sửa thông tin phiếu")
            popup.menu.add(0, 2, 0, "Xóa phiếu mượn")

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    1 -> handleEditLoan()   // Gọi hàm sửa phiếu
                    2 -> handleDeleteLoan() // Gọi hàm xóa phiếu
                }
                true
            }
            popup.show()
        }

        // 3. Setup Danh sách sách (Của từng cuốn sách bên dưới)
        bookAdapter = LoanAdapter { targetBook, action ->
            when (action) {
                "EDIT" -> handleEditBook(targetBook)//Sửa thông tin sách đã mượn
                "DELETE" -> handleDeleteBook(targetBook)//Xoá thông tin sách đã mượn
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

    // Hàm hiển thị Dialog nhỏ gọn để sửa Tên và Ngày mượn
    private fun handleEditLoan() {
        // Nạp layout nhỏ mà bạn vừa tạo
        val viewDialog = layoutInflater.inflate(R.layout.layout_dialog_edit_loan, null)
        val edtName = viewDialog.findViewById<EditText>(R.id.edtEditReaderName)
        val edtBorrowDate = viewDialog.findViewById<EditText>(R.id.edtEditBorrowDate)

        // Gắn dữ liệu cũ vào ô
        edtName.setText(currentItem.readerName)
        edtBorrowDate.setText(currentItem.borrowDate)

        // Xử lý mở lịch cho Ngày mượn
        edtBorrowDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
                val newDate = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year)
                edtBorrowDate.setText(newDate)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        // Tạo Alert Dialog có gắn layout ở trên vào
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Sửa phiếu mượn")
            .setView(viewDialog)
            .setNegativeButton("Hủy", null)
            .setPositiveButton("Lưu") { _, _ ->
                // Cập nhật dữ liệu vào currentItem
                currentItem = currentItem.copy(
                    readerName = edtName.text.toString(),
                    borrowDate = edtBorrowDate.text.toString()
                )
                // Gọi hàm cập nhật lại giao diện
                bindDataToUI()
                Toast.makeText(requireContext(), "Đã cập nhật thông tin phiếu!", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    // Hàm xử lý Xóa toàn bộ phiếu
    private fun handleDeleteLoan() {
        //Kiểm tra xem có sách nào mà người dùng chưa trả hay không
        val hasUnreturnedBooks = currentItem.borrowedBooks.any { it.status != "RETURNED" }

        //Nếu chưa không cho xóa phiếu
        if (hasUnreturnedBooks) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Không thể xóa")
                .setMessage("Người này vẫn còn sách chưa trả hoặc bị mất. Vui lòng xử lý toàn bộ sách trong phiếu trước khi xóa!")
                .setIcon(R.drawable.ic_diamond_exclamation)
                .setPositiveButton("Đã hiểu", null)
                .show()
            return
        }

        //Nếu đã trả hết thì có thể xóa phiếu
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Xóa phiếu mượn")
            .setMessage("Bạn có chắc chắn muốn xóa toàn bộ phiếu mượn của '${currentItem.readerName}' không?")
            .setNegativeButton("Hủy", null)
            .setPositiveButton("Xóa") { _, _ ->
                // --- CẬP NHẬT TẠI ĐÂY ---
                // 1. Phát tín hiệu xóa đi
                loanSharedViewModel.deletedLoanId.value = currentItem.loanId

                Toast.makeText(requireContext(), "Đã xóa phiếu mượn thành công!", Toast.LENGTH_SHORT).show()

                // 2. Quay lại màn hình chính
                findNavController().popBackStack()
            }
            .show()
    }
// ==========================================
// CÁC HÀM XỬ LÝ LOGIC CHO TỪNG CUỐN SÁCH
// ==========================================
    //Hàm edit lại thông tin của sách đã mượn bao gồm thay đổi trạng thái và gia hạn thêm
    private fun handleEditBook(targetBook: LoanDetailItemData) {
        val dialog = BottomSheetDialog(requireContext())
        val viewDialog = layoutInflater.inflate(R.layout.layout_dialog_edit_loan_detail, null)
        dialog.setContentView(viewDialog)

        //Ánh xạ view của dialog sửa thông tin
        val edtTitle = viewDialog.findViewById<EditText>(R.id.edtEditBookTitle)
        val edtAuthor = viewDialog.findViewById<EditText>(R.id.edtEditAuthor)
        val edtCategory = viewDialog.findViewById<EditText>(R.id.edtEditCategory)
        val spnStatus = viewDialog.findViewById<Spinner>(R.id.spnEditStatus)
        val edtDueDate = viewDialog.findViewById<EditText>(R.id.edtEditDueDate)
        val btnCancel = viewDialog.findViewById<Button>(R.id.btnCancelEdit)
        val btnSave = viewDialog.findViewById<Button>(R.id.btnSaveEdit)

        edtTitle.setText(targetBook.title)
        edtAuthor.setText(targetBook.author)
        edtCategory.setText(targetBook.categoryName)
        edtDueDate.setText(targetBook.dueDate)


        //Set up dữ liệu cho spinner trạng thái
        val statusAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            listOf("Đang mượn", "Đã trả", "Bị mất")
        )
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnStatus.adapter = statusAdapter

        val currentPos = when (targetBook.status) {
            "BORROWING" -> 0
            "RETURNED" -> 1
            "LOST" -> 2
            else -> 0
        }
        spnStatus.setSelection(currentPos)



        //Lắng nghe nút gia hạn nếu có thì hiện DatePicker để chọn ngày
        edtDueDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
                val newDate = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year)
                edtDueDate.setText(newDate)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        //Nếu nhấn hủy thì tắt dialog
        btnCancel.setOnClickListener { dialog.dismiss() }


        //Nếu lưu thì ghi đè toàn bộ lại dữ liệu lại cho item được chọn
        btnSave.setOnClickListener {
            val newTitle = edtTitle.text.toString()
            val newAuthor = edtAuthor.text.toString()
            val newCategory = edtCategory.text.toString()
            val newDueDate = edtDueDate.text.toString()

            val newStatusStr = when (spnStatus.selectedItemPosition) {
                1 -> "RETURNED"
                2 -> "LOST"
                else -> "BORROWING"
            }

            val newReturnDate = if (newStatusStr == "RETURNED" && targetBook.status != "RETURNED") {
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            } else if (newStatusStr != "RETURNED") {
                null
            } else {
                targetBook.returnDate
            }

            val updatedBooks = currentItem.borrowedBooks.map { book ->
                if (book.bookId == targetBook.bookId) {
                    book.copy(
                        title = newTitle,
                        author = newAuthor,
                        categoryName = newCategory,
                        dueDate = newDueDate,
                        status = newStatusStr,
                        returnDate = newReturnDate
                    )
                } else {
                    book
                }
            }.toMutableList()

            //Kiểm tra LoanDetailStatus để cập nhật cho LoanStatus
            val hasAnyBookBorrowing = updatedBooks.any {
                it.status == "BORROWING" || it.status == "LOST"
            }
            val newOverallStatus = if (hasAnyBookBorrowing) "BORROWING" else "RETURNED"

            //Lưu lại danh sách book mới và trạng thái mới đã cập nhật cho Loan
            currentItem = currentItem.copy(
                borrowedBooks = updatedBooks,
                overallStatus = newOverallStatus
            )

            //Chỉnh màu cho LoanStatus của layout chi tiết này
            editStatusUI(currentItem.overallStatus, tvStatus)
            //Cập nhật danh sách book
            bookAdapter.submitList(currentItem.borrowedBooks)

            //Hiển thị thông báo đã cập nhật và tự động tắt dialog khi đã lưu xong
            Toast.makeText(requireContext(), "Cập nhật sách thành công!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
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
        if (status == "BORROWING") {
            tvStatus.text = "Đang mượn"
            tvStatus.setTextColor(ContextCompat.getColor(context, R.color.text_status_info))
            tvStatus.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.status_info))
        } else {
            tvStatus.text = "Đã trả"
            tvStatus.setTextColor(ContextCompat.getColor(context, R.color.text_status_success))
            tvStatus.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.status_success))
        }
    }
}