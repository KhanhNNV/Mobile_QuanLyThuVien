package com.example.quanlythuvien.ui.borrow_pay

import android.app.DatePickerDialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R
import com.example.quanlythuvien.data.entity.enums.LoanDetailStatus
import com.example.quanlythuvien.ui.borrow_pay.data.LoanItemData
import com.example.quanlythuvien.viewmodel.LoanSharedViewModel
import java.text.SimpleDateFormat
import java.util.*

class LoanFragment : Fragment() {

    // Kết nối với "người vận chuyển"
    private val loanSharedViewModel: LoanSharedViewModel by activityViewModels()

    // Biến lưu trữ toàn bộ dữ liệu của phiếu mượn hiện tại đang được mở
    private lateinit var currentItem: LoanItemData

    // Thợ làm bánh (Adapter) phụ trách việc nhồi dữ liệu sách vào danh sách (RecyclerView)
    private lateinit var bookAdapter: LoanAdapter

// ==========================================
// KHAI BÁO CÁC VIEW TRÊN GIAO DIỆN (UI)
// ==========================================

    // --- Phần Header (Thanh tiêu đề trên cùng) ---
// Nút bấm hình mũi tên ở góc trái trên cùng, dùng để tắt màn hình này và quay lại
    private lateinit var btnBack: ImageButton

    // Dòng chữ tiêu đề giữa màn hình trên cùng (Vd: "Chi tiết phiếu mượn")
    private lateinit var tvHeaderTitle: TextView

    // --- Phần Thông tin chung của phiếu mượn (layoutTopInfo) ---
// Nơi hiển thị tên người mượn (Vd: "Nguyễn Văn A")
    private lateinit var tvReaderName: TextView

    // Nơi hiển thị mã số của phiếu mượn (Vd: "RV01")
    private lateinit var tvLoanId: TextView

    // Cục Tag (thẻ) hiển thị trạng thái tổng hợp của cả phiếu (Vd: "Đang mượn" màu xanh lá)
    private lateinit var tvStatus: TextView

    // Nơi hiển thị Ngày bắt đầu mượn sách
    private lateinit var tvBorrowDate: TextView

    // Nơi hiển thị Ngày hẹn trả sách (Hạn chót)
    private lateinit var tvDueDate: TextView

    // Nút bấm "Gia hạn", khi click vào sẽ mở bảng chọn ngày (DatePicker) để lùi ngày trả
    private lateinit var btnExtend: Button

    // --- Phần Danh sách sách bên dưới (layoutBottomList) ---
    private lateinit var rvBooks: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_loan, container, false)
    }
    private fun initViews(view: View) {
        // Lấy View từ layout header (thông qua id của include hoặc truy cập trực tiếp)
        btnBack = view.findViewById(R.id.btnBack)
        tvHeaderTitle = view.findViewById(R.id.tvHeaderTitle)

        // Các view thông tin chính
        tvReaderName = view.findViewById(R.id.tvDialogReaderName)
        tvLoanId = view.findViewById(R.id.tvDialogLoanId)
        tvStatus = view.findViewById(R.id.tvDialogStatus)
        tvBorrowDate = view.findViewById(R.id.tvDialogBorrowDate)
        tvDueDate = view.findViewById(R.id.tvDialogDueDate)
        btnExtend = view.findViewById(R.id.btChange)
        rvBooks = view.findViewById(R.id.rvBorrowedBooks)
    }


    //Hàm này giúp truyền dữ liệu trong LoanItem cho View
    private fun bindDataToUI() {
        tvReaderName.text = currentItem.readerName
        tvLoanId.text = currentItem.loanId.toString()
        tvBorrowDate.text = currentItem.borrowDate
        tvDueDate.text = currentItem.dueDate
        editStatusUI(currentItem.overallStatus, tvStatus)
        bookAdapter.submitList(currentItem.borrowedBooks)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)

        // 1. Setup Header
        tvHeaderTitle.text = "Chi tiết phiếu mượn"
        btnBack.setOnClickListener {
            // 1. Gói ghém dữ liệu (currentItem) đưa cho "người vận chuyển" mang về
            loanSharedViewModel.updatedLoanToSave.value = currentItem

            // 2. Quay lại màn hình danh sách trước đó
            findNavController().popBackStack()
        }

        // 2. Setup Danh sách sách
        bookAdapter = LoanAdapter { targetBook, newStatus ->
            val updatedBooks = currentItem.borrowedBooks.map { book ->

                //Kiểm tra xem có book nào được thay đổi trạng thái hau không nếu có thì cập nhật dô ds
                if (book.bookId == targetBook.bookId) {
                    val newReturnDate = if (newStatus == LoanDetailStatus.RETURNED) {
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                    } else null
                    book.copy(status = newStatus.value, returnDate = newReturnDate)
                } else {
                    book
                }
            }.toMutableList()

            //Xét tất LoanDetailStatus để cập nhật cho LoanStatus
            val hasAnyBookBorrowing = updatedBooks.any {
                it.status == LoanDetailStatus.BORROWING.value || it.status == LoanDetailStatus.LOST.value
            }
            val newOverallStatus = if (hasAnyBookBorrowing) "BORROWING" else "RETURNED"

            //Cập nhật lại dữ liệu mới cho item(LoanItem) được chọn
            currentItem = currentItem.copy(
                borrowedBooks = updatedBooks,
                overallStatus = newOverallStatus
            )
            //Edit trạng thái cho LoanStatus
            editStatusUI(currentItem.overallStatus, tvStatus)

            //cập nhật danh sách mới cho RecyclerView
            bookAdapter.submitList(currentItem.borrowedBooks)
        }
        rvBooks.layoutManager = LinearLayoutManager(requireContext())
        rvBooks.adapter = bookAdapter

        // 3. Nhận dữ liệu lúc mới mở Fragment lên
        loanSharedViewModel.selectedLoanToView.value?.let { item ->
            currentItem = item
            bindDataToUI()
        }

        // 4. Sự kiện Gia hạn
        btnExtend.setOnClickListener {
            handleExtension()
        }
    }

    //Hàm xử lý nút gia hạn
    private fun handleExtension() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            val newDueDate = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)
            currentItem = currentItem.copy(dueDate = newDueDate)
            tvDueDate.text = newDueDate
            Toast.makeText(requireContext(), "Gia hạn thành công!", Toast.LENGTH_SHORT).show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    //Hàm dịch và chỉnh màu sắc cho View dựa trên LoanStatus(bởi vì dùng enum)
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