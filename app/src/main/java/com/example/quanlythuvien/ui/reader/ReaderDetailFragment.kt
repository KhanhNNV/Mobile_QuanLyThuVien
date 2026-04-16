package com.example.quanlythuvien.ui.reader

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.NumberPicker
import androidx.appcompat.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R
import com.example.quanlythuvien.core.api.RetrofitClient
import com.example.quanlythuvien.data.model.request.ExtendMembershipExpiryRequest
import com.example.quanlythuvien.data.remote.ReaderApiService
import com.example.quanlythuvien.data.repository.ReaderRepository
import com.example.quanlythuvien.ui.borrow_pay.data.LoanItemData
import com.example.quanlythuvien.viewmodel.LoanSharedViewModel
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.getValue

class ReaderDetailFragment : Fragment(R.layout.fragment_reader_detail) {
    private lateinit var bookAdapter: ReaderDetailAdapter
    private var allDataMockReaderBook: List<MockReaderBook> = listOf()
    private val loanSharedViewModel: LoanSharedViewModel by activityViewModels()

    var onItemClick: ((MockReaderBook) -> Unit)? = null

    // Biến tạm để nhớ thông tin chờ lưu PDF
    private var pendingPdfCode = ""
    private var pendingPdfName = ""
    private var pendingPdfPhone = ""

    // Biến lưu thông tin reader hiện tại
    private var currentReaderId: Long = 0
    private var currentMembershipExpiry: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)




        //Lấy thông tin người dùng từ bundle
        var readerName = arguments?.getString("readerName")?: ""
        var readerPhone = arguments?.getString("readerPhone")?: ""
        var readerType = arguments?.getString("readerType")?: ""
        var readerBarcode = arguments?.getString("readerBarcode")?: ""

        // Lấy readerId từ bundle
        currentReaderId = arguments?.getInt("readerId", 0)?.toLong() ?: 0L
        currentMembershipExpiry = arguments?.getString("membershipExpiry") ?: ""

        //Gắn thông tin người dùng vào giao diện  View
        view.findViewById<TextView>(R.id.tvReaderName)?.text = readerName
        view.findViewById<TextView>(R.id.tvReaderInfo)?.text = readerPhone
        view.findViewById<TextView>(R.id.tvReaderStatus)?.text = readerType
        view.findViewById<TextView>(R.id.tvAvatar)?.text = readerName?.firstOrNull()?.uppercase()
        view.findViewById<TextView>(R.id.tvHeaderTitle)?.text = "Chi tiết độc giả"
        view.findViewById<View>(R.id.btnBack)?.setOnClickListener {
            findNavController().navigateUp()
        }

        //Nút 3 chấm
        view.findViewById<View>(R.id.ivMoreOption)?.setOnClickListener {
           showOptionMenu(it,readerName, readerPhone,readerType,readerBarcode,role = currentUserRole)
        }


        //Khởi tọa data MOCK cho book
        setupMockData()

        val rvBooks = view.findViewById<RecyclerView>(R.id.rvReaderBooks)

        //Xữ lý khi nhấn vào từng book
        bookAdapter = ReaderDetailAdapter {

                selectedBook ->
            // 1. Tạo hoặc lấy thông tin Phiếu mượn tương ứng với cuốn sách này
            // Vì hiện tại bạn đang dùng Mock dữ liệu, ta sẽ tạo một LoanItemData giả
            val mockLoan =LoanItemData(
                loanId = 12345, // ID thực tế bạn sẽ lấy từ dữ liệu sách hoặc phiếu
                readerName = arguments?.getString("readerName") ?: "Độc giả",
                borrowDate = selectedBook.borrowDate,
                overallStatus = if (selectedBook.isReturned) "RETURNED" else "BORROWING",
                borrowedBooks = mutableListOf() // Bạn có thể đưa danh sách sách vào đây
            )

            // 2. Lưu thông tin này vào ViewModel để trang LoanDetailFragment có thể đọc được
            loanSharedViewModel.selectedLoanToView.value = mockLoan
            //Mốt truyền dữ liệu thông qua API
            findNavController().navigate(R.id.loanFragment)


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

    //Giả sử người dùng là nhân viên
    val currentUserRole = "STAFF"
    private fun showOptionMenu(
        view: View,
        readerName: String,
        readerPhone: String,
        readerType: String,
        readerBarcode: String,
        role:String
    ) {
        //Tạo đối tượng popMenu
        val popMenu = PopupMenu(requireContext(), view)

        //Chuyển file xml sang đối tượng popMenu
        popMenu.menuInflater.inflate(R.menu.menu_reader_options, popMenu.menu)

        //Nếu là staff ẩn nút xóa
        if (role == "STAFF") {
            val deleteMenuItem = popMenu.menu.findItem(R.id.menuDeleteReader)
            deleteMenuItem?.isVisible = false
        }

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
                //  BẮT SỰ KIỆN IN PDF
                R.id.menuPrintPdf -> {

                    pendingPdfCode = readerBarcode
                    pendingPdfName = readerName
                    pendingPdfPhone = readerPhone

                    // Mở hộp thoại chọn thư mục lưu file với tên mặc định
                    createPdfLauncher.launch("TheDocGia_${pendingPdfCode}.pdf")
                    true
                }
                // Xử lý khi nhấn vào nút gia hạn
                R.id.menuExtendReader -> {
                    showExtendMembershipDialog(readerName, readerPhone)
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
            popMenu.setForceShowIcon(true)
            popMenu.show()
        }

    /**
     * Hiển thị Dialog gia hạn thẻ độc giả sử dụng NumberPicker
     */
    private fun showExtendMembershipDialog(readerName: String, readerPhone: String) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_extend_membership)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val tvExtendReaderInfo = dialog.findViewById<TextView>(R.id.tvExtendReaderInfo)
        val tvCurrentExpiryDate = dialog.findViewById<TextView>(R.id.tvCurrentExpiryDate)
        val numberPickerMonth = dialog.findViewById<NumberPicker>(R.id.numberPickerMonth)
        val tvNewExpiryDate = dialog.findViewById<TextView>(R.id.tvNewExpiryDate)
        val btnCancelExtend = dialog.findViewById<Button>(R.id.btnCancelExtend)
        val btnConfirmExtend = dialog.findViewById<Button>(R.id.btnConfirmExtend)

        // Hiển thị thông tin độc giả
        tvExtendReaderInfo.text = "$readerName - $readerPhone"

        // Hiển thị ngày hết hạn hiện tại
        val currentExpiryDisplay = if (currentMembershipExpiry.isNotEmpty()) {
            formatDisplayDate(currentMembershipExpiry)
        } else {
            "Chưa có thông tin"
        }
        tvCurrentExpiryDate.text = currentExpiryDisplay

        // Cấu hình NumberPicker
        numberPickerMonth.minValue = 1
        numberPickerMonth.maxValue = 24
        numberPickerMonth.value = 2
        numberPickerMonth.wrapSelectorWheel = false

        // Hàm cập nhật ngày hết hạn mới
        fun updateNewExpiryDate(months: Int) {
            if (currentMembershipExpiry.isNotEmpty()) {
                try {
                    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    val currentDate = sdf.parse(currentMembershipExpiry)
                    if (currentDate != null) {
                        val calendar = Calendar.getInstance()
                        calendar.time = currentDate
                        calendar.add(Calendar.MONTH, months)
                        val newDate = calendar.time
                        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        tvNewExpiryDate.text = "Ngày hết hạn mới: ${outputFormat.format(newDate)}"
                    } else {
                        tvNewExpiryDate.text = "Ngày hết hạn mới: +$months tháng"
                    }
                } catch (e: Exception) {
                    tvNewExpiryDate.text = "Ngày hết hạn mới: +$months tháng"
                }
            } else {
                tvNewExpiryDate.text = "Ngày hết hạn mới: +$months tháng"
            }
        }

        // Khởi tạo hiển thị ban đầu
        updateNewExpiryDate(2)

        // Lắng nghe sự kiện thay đổi của NumberPicker
        numberPickerMonth.setOnValueChangedListener { _, _, newVal ->
            updateNewExpiryDate(newVal)
        }

        // Xử lý nút Hủy
        btnCancelExtend.setOnClickListener {
            dialog.dismiss()
        }

        // Xử lý nút Xác nhận
        btnConfirmExtend.setOnClickListener {
            val months = numberPickerMonth.value.toLong()
            btnConfirmExtend.isEnabled = false
            btnConfirmExtend.text = "Đang xử lý..."
            extendMembership(currentReaderId, months, dialog, btnConfirmExtend)
        }

        dialog.show()
    }

    /**
     * Gọi API gia hạn thẻ độc giả
     */
    private fun extendMembership(readerId: Long, months: Long, dialog: Dialog, confirmButton: Button) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val retrofit = RetrofitClient.getInstance(requireContext())
                val apiService = retrofit.create(ReaderApiService::class.java)
                val repository = ReaderRepository(apiService)

                val request = ExtendMembershipExpiryRequest(monthRegis = months)
                val response = repository.extendMembershipExpiry(readerId, request)

                if (response.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "Gia hạn thành công! Đã gia hạn thêm $months tháng.",
                        Toast.LENGTH_LONG
                    ).show()

                    // Cập nhật lại thông tin hiển thị
                    response.body()?.let { updatedReader ->
                        currentMembershipExpiry = updatedReader.membershipExpiry ?: ""
                        view?.findViewById<TextView>(R.id.tvExpireDate)?.text = formatDisplayDate(currentMembershipExpiry)
                    }

                    dialog.dismiss()
                } else {
                    val errorMsg = when (response.code()) {
                        400 -> "Dữ liệu không hợp lệ"
                        404 -> "Không tìm thấy độc giả"
                        403 -> "Bạn không có quyền thực hiện"
                        else -> "Lỗi: ${response.code()}"
                    }
                    Toast.makeText(requireContext(), "Gia hạn thất bại: $errorMsg", Toast.LENGTH_LONG).show()
                    confirmButton.isEnabled = true
                    confirmButton.text = "XÁC NHẬN"
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Lỗi kết nối: ${e.message}", Toast.LENGTH_LONG).show()
                confirmButton.isEnabled = true
                confirmButton.text = "XÁC NHẬN"
            }
        }
    }

    /**
     * Format ngày tháng để hiển thị
     */
    private fun formatDisplayDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            dateString
        }
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

    //HÀM TẠO VÀ LƯU PDF
    private fun writePdfToUri(uri: android.net.Uri, code: String, name: String, phone: String) {
        val pdfDocument = android.graphics.pdf.PdfDocument()
        val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(400, 300, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = android.graphics.Paint()
        paint.color = android.graphics.Color.BLACK
        paint.textSize = 16f

        paint.isFakeBoldText = true
        canvas.drawText("THẺ ĐỘC GIẢ THƯ VIỆN", 100f, 50f, paint)
        paint.isFakeBoldText = false

        canvas.drawText("Mã thẻ: $code", 50f, 100f, paint)
        canvas.drawText("Họ và tên: $name", 50f, 140f, paint)
        canvas.drawText("Số điện thoại: $phone", 50f, 180f, paint)

        pdfDocument.finishPage(page)
        try {
            requireContext().contentResolver.openOutputStream(uri)?.use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
            Toast.makeText(requireContext(), "Lưu PDF thành công! Mở mục Tệp (Files) để xem.", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Lỗi khi lưu PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }
    private val createPdfLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null) {
            // Nếu người dùng chọn chỗ lưu thành công, tiến hành vẽ và ghi PDF vào chỗ đó
            writePdfToUri(uri, pendingPdfCode, pendingPdfName, pendingPdfPhone)
        } else {
            Toast.makeText(requireContext(), "Đã hủy lưu file PDF", Toast.LENGTH_SHORT).show()
        }
    }

}


