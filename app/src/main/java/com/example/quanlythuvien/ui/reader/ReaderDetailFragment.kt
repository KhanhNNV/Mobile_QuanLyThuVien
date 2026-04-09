package com.example.quanlythuvien.ui.reader

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R
import com.example.quanlythuvien.data.entity.enums.LoanDetailStatus
import com.example.quanlythuvien.ui.borrow_pay.DialogBorrowPayAdapter
import com.example.quanlythuvien.ui.borrow_pay.data.LoanDetailItemData
import com.example.quanlythuvien.ui.borrow_pay.data.LoanItemData
import com.example.quanlythuvien.utils.setupCustomHeader
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import java.security.KeyStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReaderDetailFragment : Fragment(R.layout.fragment_reader_detail) {
    private lateinit var bookAdapter: ReaderDetailAdapter
    private var allDataMockReaderBook: List<MockReaderBook> = listOf()

    var onItemClick: ((MockReaderBook) -> Unit)? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Lấy thông tin người dùng từ bundle
        var readerName = arguments?.getString("readerName")?: ""
        var readerPhone = arguments?.getString("readerPhone")?: ""
        var readerType = arguments?.getString("readerType")?: ""

        //Gắn thông tin người dùng vào giao diện  View
        view.findViewById<TextView>(R.id.tvReaderName)?.text = readerName
        view.findViewById<TextView>(R.id.tvReaderInfo)?.text = readerPhone
        view.findViewById<TextView>(R.id.tvReaderStatus)?.text = readerType
        view.findViewById<TextView>(R.id.tvAvatar)?.text = readerName?.firstOrNull()?.uppercase()
        view.findViewById<TextView>(R.id.tvHeaderTitle)?.text = "Chi tiết độc giả"
        view.findViewById<View>(R.id.btnBack)?.setOnClickListener {
            findNavController().navigateUp()
        }

        view.findViewById<View>(R.id.ivMoreOption)?.setOnClickListener {
           showOptionMenu(it,readerName, readerPhone,readerType)
        }


        //Khởi tọa data MOCK cho book
        setupMockData()

        val rvBooks = view.findViewById<RecyclerView>(R.id.rvReaderBooks)
        bookAdapter = ReaderDetailAdapter() { selectedBook ->
            val currentStatus = if (selectedBook.isReturned) "RETURNED" else "BORROWING"

            // Đóng gói selectedBook thành LoanItemData để khớp với hàm showDetailDialog
            val fakeLoanDataForDialog = LoanItemData(
                loanId = 999, // Fake ID
                borrowDate = selectedBook.borrowDate,
                dueDate = selectedBook.dueDate,
                overallStatus = currentStatus,
                readerName = readerName, // Tên độc giả bạn đã lấy ở Fragment này
                borrowedBooks = mutableListOf( // Đổi thành mutableListOf nếu List bên kia yêu cầu
                    // Tạo 1 item list chứa chính cuốn sách vừa click
                    LoanDetailItemData(
                        title = selectedBook.title,
                        author = selectedBook.author, // Bổ sung tác giả
                        categoryName = "Không có",
                        returnDate = if (selectedBook.isReturned) selectedBook.dueDate else null, // Bổ sung ngày trả
                        status = currentStatus // Bổ sung trạng thái
                    )
                )
            )

            // Gọi hàm và truyền data đã đóng gói chuẩn vào
            showDetailDialog(fakeLoanDataForDialog)
        }
        rvBooks?.layoutManager = LinearLayoutManager(requireContext())
        rvBooks?.adapter = bookAdapter

        // Logic Filter sử dụng TabLayout
        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)
        tabLayout?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> filterBooks(status = 0) // Đang mượn
                    1 -> filterBooks(status = 1) // Đã trả
                    2 -> filterBooks(status = 2) // Quá hạn
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
        view.post {
            filterBooks(status = 0)
        }
    }


    /**
     * Hiển thị PopupMenu chứa các thao tác với đối tượng Reader.
     *
     * @param readerName Tên hiển thị của Reader.
     * @param readerPhone SĐT liên lạc.
     * @param readerType Phân loại.
     */
    private fun showOptionMenu(
        view: View,
        readerName: String,
        readerPhone: String,
        readerType: String
    ) {
        //Tạo đối tượng popMenu
        val popMenu = PopupMenu(requireContext(), view)

        //Chuyển file xml sang đối tượng popMenu
        popMenu.menuInflater.inflate(R.menu.menu_reader_options, popMenu.menu)

        //Xữ lý sự kiện click
        popMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                //Xữ lý khi nhấn vào nút edit reader
                R.id.menuEditReader -> {
                    //Truyền dữ từ tham số
                    val bundle = Bundle().apply {
                        putString("readerName", readerName)
                        putString("readerPhone", readerPhone)
                        putString("readerType", readerType)
                    }
                    findNavController().navigate(R.id.actionReaderDetailToReaderAdd, bundle)
                    true
                }
                //Xữ lý khi nhấn vào nút xóa reader
                R.id.menuDeleteReader -> {
                    //Gọi hàm hiển thị dialog xác nhận xóa
                    showDeleteConfirmationDialog()
                    true
                }
                else -> false
                }
            }
            popMenu.show()
        }
    /**
     * Hiển thị Dialog xác nhận xóa Reader.
     */
    private fun showDeleteConfirmationDialog(){
        AlertDialog.Builder(requireContext())
            .setTitle("Xóa Độc Giả")
            .setMessage("Bạn có chắc chắn muốn xóa độc giả này?")
                //Nút xác nhận xóa
            .setPositiveButton("Có") { dialog, _ ->
                //Hiển thị Toast đã xóa
                Toast.makeText(requireContext(), "Đã xóa độc giả", Toast.LENGTH_SHORT).show()
                //Quay lại trang trước
                findNavController().popBackStack()
                dialog.dismiss()//Xóa cái dialog
            }
                //Nút xác nhận hủy
            .setNegativeButton("Hủy") { dialog, _ ->
                dialog.dismiss()
            }
            // (Tùy chọn) Ngăn người dùng tắt Dialog bằng cách chạm ra ngoài
            .setCancelable(false)
            .show()
    }


    private fun setupMockData() {
        allDataMockReaderBook = listOf(
            MockReaderBook(
                "Lập trình Java căn bản",
                "Trần Văn B",
                "978-111",
                "01/10/2025",
                "15/10/2025",
                isOverdue = true,
                isReturned = false
            ),
            MockReaderBook(
                "Kotlin Coroutines",
                "JetBrains",
                "978-222",
                "10/10/2025",
                "24/10/2025",
                isOverdue = false,
                isReturned = false
            ),
            MockReaderBook(
                "Cấu trúc dữ liệu & Giải thuật",
                "Nguyễn C",
                "978-333",
                "01/09/2025",
                "15/09/2025",
                isOverdue = false,
                isReturned = true
            ),
            MockReaderBook(
                "Clean Code",
                "Robert C. Martin",
                "978-444",
                "15/08/2025",
                "30/08/2025",
                isOverdue = false,
                isReturned = true
            )
        )
    }


    private fun filterBooks(status: Int) {
        // Mốt viết logic phân chia vào đây
        val filteredList = when (status) {
            0 -> allDataMockReaderBook.filter { !it.isReturned && !it.isOverdue } // Đang mượn và chưa quá hạn
            1 -> allDataMockReaderBook.filter { it.isReturned }                   // Đã trả
            2 -> allDataMockReaderBook.filter { !it.isReturned && it.isOverdue }  // Đang mượn nhưng quá hạn
            else -> allDataMockReaderBook
        }
        bookAdapter.submitList(filteredList)
    }

    private fun showDetailDialog(item: LoanItemData){

        //Nặn khuôn cho giao diện Dialog
        val dialogView = layoutInflater.inflate(R.layout.layout_dialog_loan, null)

        //Xây dựng hộp thoại và bỏ  khuông giao diện vào hộp thoại
        val alertDialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()

        //Mở Dialog lên
        alertDialog.show()

        //Canh chỉnh lại kích thước của hộp thoại
        alertDialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.95).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        //Ánh xạ các View trong Dialog Layout
        val tvReaderName = dialogView.findViewById<TextView>(R.id.tvDialogReaderName)
        val tvLoanId = dialogView.findViewById<TextView>(R.id.tvDialogLoanId)
        val tvStatus = dialogView.findViewById<TextView>(R.id.tvDialogStatus)
        val tvBorrowDate = dialogView.findViewById<TextView>(R.id.tvDialogBorrowDate)
        val tvDueDate = dialogView.findViewById<TextView>(R.id.tvDialogDueDate)
        val btnExtend = dialogView.findViewById<Button>(R.id.btChange)
        val rvBooks = dialogView.findViewById<RecyclerView>(R.id.rvBorrowedBooks)


        tvReaderName.text = item.readerName
        tvLoanId.text = "Mã phiếu: #${item.loanId}"
        tvBorrowDate.text = item.borrowDate
        tvDueDate.text = item.dueDate

        if (item.overallStatus == "BORROWING") {
            tvStatus.text = "Đang mượn"
            tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_status_info))
            tvStatus.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.status_info))
        } else {
            tvStatus.text = "Đã trả"
            tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_status_success))
            tvStatus.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.status_success))
        }

        // 4. Set Adapter cho danh sách sách
        val bookAdapter = DialogBorrowPayAdapter { book, newStatus ->
            // Không xử lý đổi trạng thái ở màn hình xem chi tiết độc giả
        }
        rvBooks.layoutManager = LinearLayoutManager(requireContext())
        rvBooks.adapter = bookAdapter
        bookAdapter.submitList(item.borrowedBooks)

        // Ở màn hình Độc giả thì ẩn nút gia hạn
        btnExtend.visibility = View.GONE

        // 5. Hiển thị lên
        alertDialog.show()
        alertDialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.95).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

}


