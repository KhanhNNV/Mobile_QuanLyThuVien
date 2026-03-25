package com.example.quanlythuvien.ui.borrow_pay

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R
import com.example.quanlythuvien.data.entity.enums.LoanDetailStatus
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.*

class BorrowPayFragment : Fragment() {

    private lateinit var adapter: BorrowPayAdapter
    private lateinit var sampleDataList: MutableList<BorrowPayItem>

    // Các view bộ lọc
    private lateinit var edtFromDate: EditText
    private lateinit var edtToDate: EditText
    private lateinit var rgStatus: RadioGroup

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_borrow_pay, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sampleDataList = getSampleData().toMutableList()

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        adapter = BorrowPayAdapter { item ->
            showDetailDialog(item)
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter.submitList(sampleDataList)

        // Ánh xạ bộ lọc
        val btnToggleFilter = view.findViewById<ImageView>(R.id.btnToggleFilter)
        val layoutFilterContainer = view.findViewById<LinearLayout>(R.id.layoutFilterContainer)
        val btnConfirmFilter = view.findViewById<Button>(R.id.btnConfirmFilter)
        val btnResetFilter = view.findViewById<Button>(R.id.btnResetFilter)

        edtFromDate = view.findViewById(R.id.edtFromDate)
        edtToDate = view.findViewById(R.id.edtToDate)
        rgStatus = view.findViewById(R.id.rgStatus)

        // Hiện/Ẩn bộ lọc
        btnToggleFilter.setOnClickListener {
            if (layoutFilterContainer.visibility == View.GONE) {
                layoutFilterContainer.visibility = View.VISIBLE
                btnToggleFilter.animate().rotation(180f).start()
            } else {
                layoutFilterContainer.visibility = View.GONE
                btnToggleFilter.animate().rotation(0f).start()
            }
        }

        edtFromDate.setOnClickListener { showDatePickerForFilter(edtFromDate) }
        edtToDate.setOnClickListener { showDatePickerForFilter(edtToDate) }

        // Nút XÁC NHẬN LỌC
        btnConfirmFilter.setOnClickListener {
            applyFilter()
        }

        // Nút ĐẶT LẠI (RESET)
        btnResetFilter.setOnClickListener {
            // Xóa chữ 2 ô ngày
            edtFromDate.text.clear()
            edtToDate.text.clear()
            // Đưa Radio về "Tất cả"
            rgStatus.check(R.id.rbOption1)
            // Hiển thị lại toàn bộ danh sách
            adapter.submitList(sampleDataList)
        }
    }

    private fun showDatePickerForFilter(editText: EditText) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            val selectedDate = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)
            editText.setText(selectedDate)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun applyFilter() {
        val fromDateStr = edtFromDate.text.toString()
        val toDateStr = edtToDate.text.toString()
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        // Lấy ngày hôm nay để so sánh trễ hạn
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val filteredList = sampleDataList.filter { item ->
            // A. Lọc theo TRẠNG THÁI
            val statusMatch = when (rgStatus.checkedRadioButtonId) {
                R.id.rbOption2 -> item.overallStatus == "BORROWING"
                R.id.rbOption3 -> {
                    // Logic TRỄ HẠN: Đang mượn VÀ Hạn trả < Hôm nay
                    try {
                        val dueDate = sdf.parse(item.dueDate)
                        item.overallStatus == "BORROWING" && dueDate != null && dueDate.before(today)
                    } catch (e: Exception) {
                        false
                    }
                }
                else -> true // Tất cả
            }

            // B. Lọc theo THỜI GIAN HẠN TRẢ (Dùng dueDate thay vì borrowDate)
            var dateMatch = true
            try {
                val itemDueDate = sdf.parse(item.dueDate)
                if (itemDueDate != null) {
                    if (fromDateStr.isNotEmpty() && toDateStr.isNotEmpty()) {
                        val fromDate = sdf.parse(fromDateStr)
                        val toDate = sdf.parse(toDateStr)
                        if (fromDate != null && toDate != null) {
                            dateMatch = !itemDueDate.before(fromDate) && !itemDueDate.after(toDate)
                        }
                    } else if (fromDateStr.isNotEmpty()) {
                        val fromDate = sdf.parse(fromDateStr)
                        if (fromDate != null) dateMatch = !itemDueDate.before(fromDate)
                    } else if (toDateStr.isNotEmpty()) {
                        val toDate = sdf.parse(toDateStr)
                        if (toDate != null) dateMatch = !itemDueDate.after(toDate)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Kết hợp cả hai điều kiện
            statusMatch && dateMatch
        }

        adapter.submitList(filteredList)
        if (filteredList.isEmpty()) {
            Toast.makeText(requireContext(), "Không tìm thấy phiếu mượn phù hợp!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDetailDialog(item: BorrowPayItem) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_layout_detail, null)
        val alertDialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()

        alertDialog.show()

        alertDialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.95).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val tvReaderName = dialogView.findViewById<TextView>(R.id.tvDialogReaderName)
        val tvReaderId = dialogView.findViewById<TextView>(R.id.tvDialogReaderId)
        val tvStatus = dialogView.findViewById<TextView>(R.id.tvDialogStatus)
        val tvBorrowDate = dialogView.findViewById<TextView>(R.id.tvDialogBorrowDate)
        val tvDueDate = dialogView.findViewById<TextView>(R.id.tvDialogDueDate)
        val btnExtend = dialogView.findViewById<Button>(R.id.btChange)
        val rvBooks = dialogView.findViewById<RecyclerView>(R.id.rvBorrowedBooks)

        tvReaderName.text = item.readerName
        tvReaderId.text = "Mã độc giả: ${item.readerId}"
        tvBorrowDate.text = "Ngày mượn: ${item.borrowDate}"
        tvDueDate.text = "Hạn trả: ${item.dueDate}"
        updateStatusUI(item.overallStatus, tvStatus)

        val bookAdapter = BorrowedBookDetailAdapter { book, newStatus ->
            book.status = newStatus.value

            if (newStatus == LoanDetailStatus.RETURNED) {
                book.returnDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            } else {
                book.returnDate = null
            }

            checkAndUpdateOverallStatus(item)

            rvBooks.adapter?.notifyDataSetChanged()
            updateStatusUI(item.overallStatus, tvStatus)

            // Dùng applyFilter() thay vì notifyDataSetChanged để bộ lọc vẫn đúng tác dụng
            applyFilter()
        }

        rvBooks.layoutManager = LinearLayoutManager(requireContext())
        rvBooks.adapter = bookAdapter
        bookAdapter.submitList(item.borrowedBooks)

        btnExtend.setOnClickListener {
            handleExtension(item, tvDueDate)
        }
    }

    private fun checkAndUpdateOverallStatus(item: BorrowPayItem) {
        val hasAnyBookBorrowing = item.borrowedBooks.any {
            it.status == LoanDetailStatus.BORROWING.value
        }

        if (hasAnyBookBorrowing) {
            item.overallStatus = "BORROWING"
        } else {
            item.overallStatus = "RETURNED"
        }
    }

    private fun updateStatusUI(status: String, tvStatus: TextView) {
        if (status == "BORROWING") {
            tvStatus.text = "ĐANG MƯỢN"
            tvStatus.backgroundTintList = android.content.res.ColorStateList.valueOf(
                androidx.core.content.ContextCompat.getColor(requireContext(), android.R.color.holo_orange_dark)
            )
        } else {
            tvStatus.text = "ĐÃ TRẢ"
            tvStatus.backgroundTintList = android.content.res.ColorStateList.valueOf(
                androidx.core.content.ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark)
            )
        }
    }

    private fun handleExtension(item: BorrowPayItem, tvUpdate: TextView) {
        val calendar = Calendar.getInstance()
        android.app.DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            val newDueDate = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)
            item.dueDate = newDueDate
            tvUpdate.text = "Hạn trả: $newDueDate"

            // Dùng applyFilter() thay vì notifyDataSetChanged để list tự làm mới lại theo bộ lọc
            applyFilter()

            Toast.makeText(requireContext(), "Gia hạn thành công!", Toast.LENGTH_SHORT).show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun getSampleData(): List<BorrowPayItem> {
        return listOf(
            BorrowPayItem(1, "10/03/2026", "20/03/2026", "BORROWING", "RD001", "Nguyễn Văn A",
                mutableListOf(BorrowedBookDetail("Lập trình Java", "James Gosling", "Kỹ thuật", null, "BORROWING"))
            ),
            BorrowPayItem(2, "05/03/2026", "15/03/2026", "RETURNED", "RD002", "Trần Thị B",
                mutableListOf(BorrowedBookDetail("Android nâng cao", "Google", "Lập trình", "14/03/2026", "RETURNED"))
            ),
            BorrowPayItem(3, "01/03/2026", "10/03/2026", "BORROWING", "RD003", "Người Trễ Hạn",
                mutableListOf(BorrowedBookDetail("Clean Code", "Robert C. Martin", "Kỹ thuật", null, "BORROWING"))
            )
        )
    }
}